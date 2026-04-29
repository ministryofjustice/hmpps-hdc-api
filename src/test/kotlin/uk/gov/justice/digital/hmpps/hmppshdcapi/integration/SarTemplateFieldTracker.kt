package uk.gov.justice.digital.hmpps.hmppshdcapi.integration

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.github.jknack.handlebars.Context
import com.github.jknack.handlebars.Handlebars
import com.github.jknack.handlebars.Options
import com.github.jknack.handlebars.io.ClassPathTemplateLoader
import uk.gov.justice.digital.hmpps.hmppshdcapi.util.StringListHolder
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import kotlin.reflect.KClass
import kotlin.reflect.KType
import kotlin.reflect.full.memberProperties

/**
 * Checks that every leaf field declared in the SAR data model (via Kotlin reflection) is
 * accessed – i.e. rendered – when the Handlebars template is executed against the test data.
 *
 * Two categories of result:
 *  • unaccessedPaths     – in model + has test data, but never accessed by the template (coverage gap)
 *  • pathsWithNoTestData – in model but always null in test data; populate test data or add to ignoredPaths
 *
 * ignoredPaths supports prefix matching: ignoring "foo.bar" also covers "foo.bar.baz".
 */
object SarTemplateFieldTracker {

  data class Result(
    val unaccessedPaths: List<String>,
    val pathsWithNoTestData: List<String>,
    val expectedPaths: Set<String>,
  )

  /** Types treated as leaves – reflection stops here and records the property path. */
  private val LEAF_TYPES: Set<KClass<*>> = setOf(
    String::class,
    StringListHolder::class,
    LocalDateTime::class,
    LocalDate::class,
    LocalTime::class,
    Map::class,
    MutableMap::class,
  )

  fun check(content: Any?, modelClass: KClass<*>, ignoredPaths: Set<String> = emptySet()): Result {
    val expectedPaths = reflectionLeafPaths(modelClass).map(::normalizePath).toSet()

    val mapper = jacksonObjectMapper()

    @Suppress("UNCHECKED_CAST")
    val dataMap = mapper.readValue(mapper.writeValueAsString(content), Map::class.java) as Map<String, Any?>
    val dataPaths = allLeafPaths(dataMap, "").map(::normalizePath).toSet()

    val accessedPaths = mutableSetOf<String>()
    buildHandlebars()
      .compile("sar_hmpps-hdc-api.mustache")
      .apply(Context.newContext(TrackingMap("", dataMap, accessedPaths)))
    val normalizedAccessedPaths = accessedPaths.map(::normalizePath).toSet()

    fun isIgnored(path: String) = ignoredPaths.any { ig -> path == ig || path.startsWith("$ig.") || path.startsWith("$ig[") }

    return Result(
      expectedPaths = expectedPaths,
      unaccessedPaths = (expectedPaths intersect dataPaths)
        .filterNot { it in normalizedAccessedPaths || isIgnored(it) }.sorted(),
      pathsWithNoTestData = (expectedPaths - dataPaths)
        .filterNot(::isIgnored).sorted(),
    )
  }

  private fun buildHandlebars(): Handlebars {
    val handlebars = Handlebars(ClassPathTemplateLoader("/templates", ""))
    handlebars.registerHelper<Any?>("optionalValue") { value, _ -> value?.toString() ?: "" }
    handlebars.registerHelper<Any?>("formatDate") { value, _ -> value?.toString() ?: "" }
    handlebars.registerHelper<Any?>("helperMissing") { _, options: Options -> options.params.firstOrNull()?.toString() ?: "" }
    return handlebars
  }

  internal fun normalizePath(path: String) = path.replace(Regex("\\[\\d+]"), "[]")

  /**
   * Recursively enumerates all leaf-property paths from [kClass].
   * Sealed interfaces are expanded to the union of all concrete subclasses.
   * [visited] prevents infinite recursion from circular references.
   */
  internal fun reflectionLeafPaths(kClass: KClass<*>, prefix: String = "", visited: MutableSet<KClass<*>> = mutableSetOf()): Set<String> {
    if (!visited.add(kClass)) return emptySet()
    try {
      return kClass.memberProperties.flatMap { prop ->
        val path = if (prefix.isEmpty()) prop.name else "$prefix.${prop.name}"
        pathsForType(prop.returnType, path, visited)
      }.toSet()
    } finally {
      visited.remove(kClass)
    }
  }

  private fun isLeaf(kClass: KClass<*>) = kClass in LEAF_TYPES || kClass.java.isEnum || kClass.javaPrimitiveType != null

  private fun pathsForType(kType: KType, path: String, visited: MutableSet<KClass<*>>): Set<String> {
    val classifier = kType.classifier as? KClass<*> ?: return setOf(path)
    return when {
      isLeaf(classifier) -> setOf(path)
      classifier == List::class || classifier == MutableList::class || classifier == Collection::class -> {
        val elementClassifier = kType.arguments.firstOrNull()?.type?.classifier as? KClass<*> ?: return setOf(path)
        // Primitive/simple list (e.g. List<String>) – treat as a single leaf.
        if (isLeaf(elementClassifier)) setOf(path) else reflectionLeafPaths(elementClassifier, "$path[]", visited)
      }
      classifier.java.isSealed ->
        classifier.java.permittedSubclasses
          .flatMap { reflectionLeafPaths(it.kotlin, path, visited) }.toSet()
      classifier.memberProperties.isNotEmpty() -> reflectionLeafPaths(classifier, path, visited)
      else -> setOf(path)
    }
  }

  /**
   * Collects all non-null leaf paths from a Jackson-deserialised Map structure.
   * A list of primitives is treated as a single leaf (mirrors StringListHolder's reflection treatment).
   * StringListHolder fields can serialize as either a single string or an array of strings,
   * both should be recognized as the same leaf path.
   */
  internal fun allLeafPaths(obj: Any?, prefix: String): Set<String> = when {
    obj == null -> emptySet()
    obj is Map<*, *> ->
      @Suppress("UNCHECKED_CAST")
      (obj as Map<String, Any?>).flatMap { (k, v) ->
        allLeafPaths(v, if (prefix.isEmpty()) k else "$prefix.$k")
      }.toSet()
    obj is List<*> -> {
      val nonNull = obj.filterNotNull()
      // Empty list or list of primitives (including StringListHolder serialized as array) → single leaf
      if (nonNull.isEmpty() || nonNull.none { it is Map<*, *> || it is List<*> }) {
        if (prefix.isEmpty()) emptySet() else setOf(prefix)
      } else {
        // List of complex objects → recurse into each element
        obj.flatMapIndexed { i, el -> allLeafPaths(el, "$prefix[$i]") }.toSet()
      }
    }
    else -> if (prefix.isEmpty()) emptySet() else setOf(prefix)
  }

  private fun wrap(path: String, value: Any?, tracker: MutableSet<String>): Any? = when (value) {
    is Map<*, *> ->
      @Suppress("UNCHECKED_CAST")
      TrackingMap(path, value as Map<String, Any?>, tracker)
    is List<*> -> TrackingList(path, value, tracker)
    else -> value
  }

  /** A field only counts as used when it resolves to a non-empty string or any other non-null value. */
  private fun shouldTrackAccessedValue(value: Any?): Boolean = when (value) {
    null -> false
    is String -> value.isNotEmpty()
    else -> true
  }

  /**
   * Intercepts map.get(key) during Handlebars rendering to record every accessed field path.
   * Handlebars' MapValueResolver resolves properties by calling map.get(name).
   */
  private class TrackingMap(
    private val path: String,
    private val delegate: Map<String, Any?>,
    private val tracker: MutableSet<String>,
  ) : Map<String, Any?> by delegate {
    override fun get(key: String): Any? {
      val childPath = if (path.isEmpty()) key else "$path.$key"
      val value = delegate[key]
      if (shouldTrackAccessedValue(value)) tracker.add(childPath)
      return wrap(childPath, value, tracker)
    }
  }

  /** Wraps list elements so field accesses on iterated items are also recorded. */
  private class TrackingList(
    private val path: String,
    private val delegate: List<Any?>,
    private val tracker: MutableSet<String>,
  ) : List<Any?> by delegate {
    private fun wrapAt(index: Int, value: Any?) = wrap("$path[$index]", value, tracker)

    override fun iterator() = object : Iterator<Any?> {
      private val inner = delegate.iterator()
      private var i = 0
      override fun hasNext() = inner.hasNext()
      override fun next() = wrapAt(i++, inner.next())
    }

    override fun listIterator() = listIteratorFrom(delegate.listIterator(), 0)
    override fun listIterator(index: Int) = listIteratorFrom(delegate.listIterator(index), index)

    private fun listIteratorFrom(inner: ListIterator<Any?>, start: Int) = object : ListIterator<Any?> {
      private var i = start
      override fun hasNext() = inner.hasNext()
      override fun hasPrevious() = inner.hasPrevious()
      override fun next() = wrapAt(i, inner.next()).also { i++ }
      override fun previous() = wrapAt(--i, inner.previous())
      override fun nextIndex() = inner.nextIndex()
      override fun previousIndex() = inner.previousIndex()
    }
  }
}
