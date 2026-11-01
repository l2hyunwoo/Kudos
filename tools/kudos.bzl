"""Bazel counterparts of the Gradle convention plugins in build-logic/.

kudos_library = kudos.kotlin.multiplatform (+ kudos.compose.multiplatform / kudos.feature / kudos.ktor)
for the Android target only: commonMain + androidMain compile as one kt_android_library.
"""

load("@rules_kotlin//kotlin:android.bzl", "kt_android_library")

_COMPOSE_DEPS = [
    "@maven//:androidx_compose_runtime_runtime_android",
    "@maven//:androidx_compose_foundation_foundation_android",
    "@maven//:androidx_compose_ui_ui_android",
    "@maven//:org_jetbrains_compose_components_components_resources_android",
    "@maven//:androidx_compose_ui_ui_tooling_preview_android",
    "@maven//:androidx_compose_ui_ui_tooling_android",
    "@maven//:androidx_compose_material_material_icons_extended_android",
    "@maven//:androidx_compose_material3_material3_android",
]

_KTOR_DEPS = [
    "@maven//:org_jetbrains_kotlinx_kotlinx_serialization_json_jvm",
    "@maven//:io_ktor_ktor_client_core_jvm",
    "@maven//:io_ktor_ktor_client_content_negotiation_jvm",
    "@maven//:io_ktor_ktor_serialization_kotlinx_json_jvm",
    "@maven//:io_ktor_ktor_client_logging_jvm",
    "@maven//:de_jensklingenberg_ktorfit_ktorfit_lib_android",
    "@maven//:io_ktor_ktor_client_okhttp_jvm",
    "@maven//:org_slf4j_slf4j_android",
]

_FEATURE_DEPS = [
    "//core/common",
    "//core/design",
    "//core/soil",
    "@maven//:androidx_navigation_navigation_compose_android",
    "@maven//:androidx_lifecycle_lifecycle_runtime_compose_android",
]

def _compose_resources(name, package):
    """Runs the Res generator. Outputs are known from the source glob, so no tree artifacts."""
    srcs = native.glob(["src/commonMain/composeResources/**"])
    if not srcs:
        return None, []
    root = "src/commonMain/composeResources/"
    assets_dir = name + "_res"
    outs = []
    for src in srcs:
        rel = src[len(root):]
        if rel.startswith("values") and rel.endswith(".xml"):
            rel = rel[:-len(".xml")] + ".commonMain.cvr"
        outs.append("%s/composeResources/%s/%s" % (assets_dir, package, rel))
    srcjar = name + "_res.srcjar"
    native.genrule(
        name = name + "_res_gen",
        srcs = srcs,
        outs = [srcjar] + outs,
        cmd = ("$(execpath //tools:compose_resources_gen) --res_dir $$(dirname $(execpath %s))/.. " % srcs[0] +
               "--package %s --srcjar $(execpath %s) --assets_root $(RULEDIR)/%s" % (package, srcjar, assets_dir)),
        tools = ["//tools:compose_resources_gen"],
    )
    return srcjar, outs

def kudos_library(
        name,
        custom_package,
        deps = [],
        exports = [],
        compose = False,
        feature = False,
        ktor = False,
        serialization = True,
        **kwargs):
    """One Gradle KMP module, Android target.

    Args:
      name: target name (the module directory name).
      custom_package: Gradle `android { namespace }`.
      deps: module and maven deps (Gradle commonMain/androidMain `implementation`).
      exports: Gradle `api` deps.
      compose: kudos.compose.multiplatform (Compose compiler + CMP deps).
      feature: kudos.feature (implies compose, adds core:common/design/soil + navigation).
      ktor: kudos.ktor (serialization + ktor + Ktorfit KSP).
      serialization: kotlinx-serialization compiler plugin (kudos.kotlin.multiplatform applies it).
      **kwargs: passed to kt_android_library.
    """
    compose = compose or feature
    plugins = ["//tools:metro_compiler"]
    all_deps = list(deps) + ["@maven//:dev_zacsweers_metro_runtime_jvm"]
    if serialization or ktor:
        plugins.append("//tools:serialization_compiler")
    if compose:
        plugins.append("//tools:compose_compiler")
        all_deps += _COMPOSE_DEPS
    if feature:
        all_deps += [d for d in _FEATURE_DEPS if d != "//" + native.package_name()]
    if ktor:
        plugins.append("//tools:ktorfit_ksp")
        all_deps += _KTOR_DEPS

    pkg = "kudos." + native.package_name().replace("/", ".") + ".generated.resources"
    res_srcjar, assets = _compose_resources(name, pkg)
    srcs = native.glob(["src/commonMain/kotlin/**/*.kt", "src/androidMain/kotlin/**/*.kt"])
    if res_srcjar:
        srcs.append(res_srcjar)

    kt_android_library(
        name = name,
        srcs = srcs,
        custom_package = custom_package,
        manifest = "//tools:AndroidManifest.xml",
        assets = assets,
        assets_dir = (name + "_res") if assets else None,
        plugins = plugins,
        deps = _dedupe(all_deps),
        exports = exports,
        visibility = ["//visibility:public"],
        **kwargs
    )

def _dedupe(items):
    out = []
    for i in items:
        if i not in out:
            out.append(i)
    return out
