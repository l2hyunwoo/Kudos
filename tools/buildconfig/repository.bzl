"""Generate local network configuration with Java Properties parsing."""

def _local_buildconfig_impl(ctx):
    properties = ctx.path(ctx.attr.root_module).dirname.get_child("local.properties")
    ctx.watch(properties)
    ctx.watch(ctx.path(ctx.attr._generator))
    java_home = ctx.getenv("JAVA_HOME")
    java = ctx.path(java_home).get_child("bin/java") if java_home else ctx.which("java")
    if not java:
        fail("BuildConfig generation requires JDK 11+ on PATH or JAVA_HOME")
    result = ctx.execute([
        java,
        ctx.path(ctx.attr._generator),
        properties,
        ctx.path("BuildConfig.kt"),
    ], quiet = True)
    if result.return_code:
        fail("BuildConfig generation failed; check JDK 11+ and local.properties syntax/permissions")
    ctx.file("BUILD.bazel", 'filegroup(name = "buildconfig", srcs = ["BuildConfig.kt"], visibility = ["//visibility:public"])\n')

local_buildconfig = repository_rule(
    implementation = _local_buildconfig_impl,
    attrs = {
        "root_module": attr.label(mandatory = True, allow_single_file = True),
        "_generator": attr.label(default = Label("//tools/buildconfig:GenerateBuildConfig.java"), allow_single_file = True),
    },
    local = True,
    configure = True,
)
