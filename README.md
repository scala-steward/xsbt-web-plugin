[![Build status](https://github.com/earldouglas/xsbt-web-plugin/workflows/build/badge.svg)](https://github.com/earldouglas/xsbt-web-plugin/actions)
[![Latest version](https://img.shields.io/github/tag/earldouglas/xsbt-web-plugin.svg)](https://index.scala-lang.org/earldouglas/xsbt-web-plugin)

# xsbt-web-plugin

xsbt-web-plugin is an [sbt](https://www.scala-sbt.org/) plugin for
building web applications with [Java
servlets](https://en.wikipedia.org/wiki/Java_servlet).

## Features

* Package a project as a *.war* file
* Test and run under Jetty or Tomcat
* Deploy directly to Heroku or AWS
* Supports sbt 1.3.0+ and 0.13.6 - 0.13.18
* Supports Scala 2.10.2+

## Getting help

* Look for *earldouglas* in [sbt/sbt](https://gitter.im/sbt/sbt) on Gitter
* Use the [*xsbt-web-plugin* tag](https://stackoverflow.com/questions/tagged/xsbt-web-plugin) on Stack Overflow
* Submit a bug report or feature request as a [new GitHub issue](https://github.com/earldouglas/xsbt-web-plugin/issues/new)
* See examples in [src/sbt-test/examples/](src/sbt-test/examples/)

## Quick reference

Add xsbt-web-plugin to *project/plugins.sbt*:

```scala
addSbtPlugin("com.earldouglas" % "xsbt-web-plugin" % "4.2.4")
```

Enable the Jetty plugin:

*build.sbt*:

```scala
enablePlugins(JettyPlugin)
```

From the sbt console:

* Start (or restart) the container with `jetty:start`
* Stop the container with `jetty:stop`
* Build a *.war* file with `package`

To use Tomcat instead of Jetty:

* Substitute `TomcatPlugin` for `JettyPlugin`
* Substitute `tomcat:start` for `jetty:start`
* Substitute `tomcat:stop` for `jetty:stop`

## Starting with Giter8

```
sbt new earldouglas/xsbt-web-plugin.g8
```

## Starting from scratch

Create a new empty project:

```
mkdir myproject
cd myproject
```

Set up the project structure:

```
mkdir project
mkdir -p src/main/scala/mypackage
```

Configure sbt:

*project/build.properties:*

```
sbt.version=1.6.1
```

*project/plugins.sbt:*

```scala
addSbtPlugin("com.earldouglas" % "xsbt-web-plugin" % "4.2.4")
```

*build.sbt:*

```scala
scalaVersion := "3.0.1"
libraryDependencies += "javax.servlet" % "javax.servlet-api" % "3.0.1" % "provided"
enablePlugins(TomcatPlugin)
```

Add a servlet:

*src/main/scala/mypackage/MyServlet.scala*:

```scala
package mypackage

import javax.servlet.annotation.WebServlet
import javax.servlet.http.HttpServlet
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

@WebServlet(urlPatterns = Array("/hello"))
class MyServlet extends HttpServlet:
  println("HEY")
  override def doGet(req: HttpServletRequest, res: HttpServletResponse): Unit =
    res.setContentType("text/html")
    res.setCharacterEncoding("UTF-8")
    res.getWriter.write("""<h1>Hello, world!</h1>""")
```

Run it with `tomcat:start`:

```
$ sbt
> tomcat:start
```

```
$ curl localhost:8080/hello
<h1>Hello, world!</h1>
```

## Configuration and use

### Triggered execution

xsbt-web-plugin supports sbt's [triggered
execution](http://www.scala-sbt.org/1.0/docs/Triggered-Execution.html)
by prefixing commands with `~`.

*sbt console:*

```
> ~jetty:start
```

This starts the Jetty container, then monitors the sources, resources,
and webapp directories for changes, which triggers a container restart.

### Testing

To run a projects tests against a running instance of the webapp, use
`<container>:quicktest` or `<container>:test`:

```
> ~jetty:quicktest
```

### Container arguments

To pass extra arguments to the Jetty or Tomcat container, set
`containerArgs`:

```scala
containerArgs := Seq("--path", "/myservice")
```

* For available Jetty arguments, see the [Jetty Runner
  docs](https://www.eclipse.org/jetty/documentation/current/runner.html#_full_configuration_reference)
* For available Tomcat arguments, see [webapp-runner#options](https://github.com/heroku/webapp-runner#options)

### Custom container

To use a custom J2EE container, e.g. a main class named `runner.Run`,
enable `ContainerPlugin` and set `containerLibs` and
`containerLaunchCmd`:

```scala
enablePlugins(ContainerPlugin)

containerLibs in Container := Seq(
    "org.eclipse.jetty" %  "jetty-webapp" % "9.1.0.v20131115"
  , "org.eclipse.jetty" %  "jetty-plus"   % "9.1.0.v20131115"
  , "test"              %% "runner"       % "0.1.0-SNAPSHOT"
)

containerLaunchCmd in Container :=
  { (port, path) => Seq("runner.Run", port.toString, path) }
```

*sbt:*

```
> container:start
> container:stop
```

### Forked JVM options

To set system properties for the forked container JVM, set
`containerForkOptions`:

```scala
containerForkOptions := new ForkOptions(runJVMOptions = Seq("-Dh2g2=42"))
```

Alternatively, set `javaOptions` in the `Jetty` (or `Tomcat`)
configuration:

```scala
javaOptions in Jetty += "-Dh2g2=42"
```

To attach a debugger, set `-Xdebug` and `-Xrunjdwp`:

*build.sbt:*

```scala
javaOptions in Jetty ++= Seq(
  "-Xdebug",
  "-Xrunjdwp:transport=dt_socket,server=y,suspend=n,address=8000"
)
```

In Eclipse:

* Create and run a new *Remote Java Application* launch configuration
* Set *Connection Type* to *Scala debugger (Socket Attach)*
* Configure to connect to *localhost* on port *8000*

In IntelliJ IDEA:

* Add a Remote run configuration: *Run* -> *Edit Configurations...*
* Under *Defaults* select *Remote* and push `+` to add a new
  configuration
* By default the configuration uses port 5005; update it to 8000 as
  above
* Name this configuration, and run it in debug mode

### Debug mode

To enable debugging through
[JDWP](https://docs.oracle.com/javase/8/docs/technotes/guides/troubleshoot/introclientissues005.html),
use `jetty:debug` or `tomcat:debug`.  Optionally set `debugAddress`,
which defaults to `"debug"` under Windows and `"8888"` otherwise, and
`debugOptions`, which defaults to:

```scala
port =>
  Seq( "-Xdebug"
     , Seq( "-Xrunjdwp:transport=dt_socket"
          , "address=" + port
          , "server=y"
          , "suspend=n"
          ).mkString(",")
     )
```

### Jetty version

By default, [Jetty
Runner](https://www.eclipse.org/jetty/documentation/current/runner.html)
9.4.42 is used.  To use a different version, set
`containerLibs`:

```scala
containerLibs in Jetty := Seq("org.mortbay.jetty" % "jetty-runner" % "7.0.0.v20091005" intransitive())
```

Depending on the version, it may also be necessary to specify the name
of Jetty's runner:

```scala
containerMain := "org.mortbay.jetty.runner.Runner"
```

### Container port

By default, the container runs on port *8080*.  To use a different port,
set `containerPort`:

```scala
containerPort := 9090
```

### *jetty.xml*

To use a *jetty.xml* configuration file, set `--config` in
`containerArgs`:

```scala
containerArgs := Seq("--config", "/path/to/jetty.xml")
```

This option can be used to enable SSL and HTTPS.

### Tomcat version

By default, [Webapp Runner](https://github.com/heroku/webapp-runner)
9.0.41.0 is used.  To use a different version, set `containerLibs`:
41
```scala
containerLibs in Tomcat := Seq("com.heroku" % "webapp-runner" % "8.5.61.0" intransitive())
```

Depending on the version, it may also be necessary to specify the name
of Tomcat's runner:

```scala
containerMain in Tomcat := "webapp.runner.launch.Main"
```

### Extra container libraries

Tomcat's webapp-runner does not ship with all of the libraries that can
be found in a complete Tomcat installation.  To include extras, use
`containerLibs in Tomcat`:

```scala
containerLibs in Tomcat += "org.apache.tomcat" % "tomcat-jdbc" % "8.5.15"
```

### Renaming the *.war* file

This can be useful for keeping the version number out of the *.war* file
name, using a non-conventional file name or path, adding additional
information to the file name, etc.

```scala
artifactName := { (v: ScalaVersion, m: ModuleID, a: Artifact) =>
  a.name + "." + a.extension
}
```
See ["Modifying default
artifacts"](http://www.scala-sbt.org/1.0/docs/Artifacts.html#Modifying+default+artifacts)
in the sbt documentation for additional information.

### Massaging the *.war* file

After the *<project>/target/webapp* directory is prepared, it can be
modified with an arbitrary `File => Unit` function by setting
`webappPostProcess`.

To list the contents of the *webapp* directory after it is prepared:

```scala
webappPostProcess := {
  val log = streams.value.log
  webappDir: File =>
    def listFiles(level: Int)(f: File): Unit = {
      val indent = ((1 until level) map { _ => "  " }).mkString
      if (f.isDirectory) {
        log.info(indent + f.getName + "/")
        f.listFiles foreach { listFiles(level + 1) }
      } else log.info(indent + f.getName)
    }
    listFiles(1)(webappDir)
}
```

To include webapp resources from multiple directories in the prepared
*webapp* directory:

```scala
webappPostProcess := {
  webappDir: File =>
    val baseDir = baseDirectory.value / "src" / "main"
    IO.copyDirectory(baseDir / "webapp1", webappDir)
    IO.copyDirectory(baseDir / "webapp2", webappDir)
    IO.copyDirectory(baseDir / "webapp3", webappDir)
}
```

### Custom resources directory

Files in the extra resource directory are not compiled, and are bundled
directly in the project artifact *.jar* file.

To add a custom resources directory, set `unmanagedResourceDirectories`:

```scala
unmanagedResourceDirectories in Compile += (sourceDirectory in Compile).value / "extra"
```

### Custom sources directory

Scala files in the extra source directory are compiled, and bundled in
the project artifact *.jar* file.

To add a custom sources directory, set `unmanagedSourceDirectories`:

```scala
unmanagedSourceDirectories in Compile += (sourceDirectory in Compile).value / "extra"
```

### Utilizing *WEB-INF/classes*

By default, project classes are packaged into a *.jar* file, shipped in
the *WEB-INF/lib* directory of the *.war* file.  To instead keep them
extracted in *WEB-INF/classes*, set `webappWebInfClasses`:

```scala
webappWebInfClasses := true
```

### Web application destination

The web application destination directory is where the static Web
content, compiled Scala classes, library *.jar* files, etc. are placed.
By default, they go to *<project>/target/webapp*.

To specify a different directory, set `target` in the `webappPrepare`
configuration:

```scala
target in webappPrepare := target.value / "WebContent"
```

### Web application resources

The web application resources directory is where static Web content
(including *.html*, *.css*, and *.js* files, the *web.xml* container
configuration file, etc.  By default, this is kept in
*<project>/src/main/webapp*.

To specify a different directory, set `sourceDirectory` in the
`webappPrepare` configuration:

```scala
sourceDirectory in webappPrepare := (sourceDirectory in Compile).value / "WebContent"
```

### Prepare the web application for execution and deployment

For situations when the prepared *<project>/target/webapp* directory is
needed, but the packaged *.war* file isn't.

*sbt console:*

```
webappPrepare
```

### Add manifest attributes

Manifest attributes of the *.war* file can be configured via
`packageOptions in sbt.Keys.package` in *build.sbt*:

```scala
packageOptions in sbt.Keys.`package` +=
  Package.ManifestAttributes( java.util.jar.Attributes.Name.SEALED -> "true" )
```

### Inherit manifest attributes

To configure the *.war* file to inherit the manifest attributes of the
*.jar* file, typically set via `packageOptions in (Compile,
packageBin)`, set `inheritJarManifest` to `true`:

```scala
inheritJarManifest := true
```

### Container shutdown and sbt

By default, sbt will shutdown the running container when exiting sbt.

To allow the container to continue running after sbt exits, set
`containerShutdownOnExit`:

```scala
containerShutdownOnExit := false
```

### Deploying to Heroku

Enable the `HerokuDeploy` plugin and configure your app name:

```scala
enablePlugins(HerokuDeploy)

herokuAppName := "my-heroku-app"
```

Either install the [Heroku Toolbelt](https://toolbelt.heroku.com/), or
set your Heroku API key as an environment variable, launch sbt, and
deploy with `herokuDeploy`:

```
$ HEROKU_API_KEY="xxx-xxx-xxxx" sbt
> herokuDeploy
```

Check out your deployed application at
`https://my-heroku-app.herokuapp.com`.

### Deploying to Elastic Beanstalk

Before trying to deploy anything, create an application and a
Tomcat-based environment for it in Elastic Beanstalk.

Enable the `ElasticBeanstalkDeployPlugin` plugin, and configure your
application's name, environment, and region:

```scala
enablePlugins(ElasticBeanstalkDeployPlugin)

elasticBeanstalkAppName := "my-elastic-beanstalk-app"

elasticBeanstalkEnvName := "production"

elasticBeanstalkRegion  := "us-west-1"
```

Add AWS credentials to your environment, launch sbt, and deploy with
`elasticBeanstalkDeploy`:

```
$ AWS_ACCESS_KEY="xxx" AWS_SECRET_KEY="xxx" sbt
> elasticBeanstalkDeploy
```

Check out your deployed application at
`http://my-elastic-beanstalk-app.us-west-1.elasticbeanstalk.com`.

### Block sbt on running container

To start the container from the command line and block sbt from exiting
prematurely, use `jetty:join`:

```
$ sbt jetty:start jetty:join
```

This is useful for running sbt in production (e.g. in a Docker
container).

### Build and run from Docker

Choose a Docker image that has sbt installed, and use `sbt
<container>:start <container>:join`:

Using [hseeberger/scala-sbt](https://hub.docker.com/r/hseeberger/scala-sbt/);

```
$ docker run -p 8080:8080 -v $PWD:/root hseeberger/scala-sbt sbt tomcat:start tomcat:join
```

Using [bigtruedata/sbt](https://hub.docker.com/r/bigtruedata/sbt/):

```
$ docker run -p 8080:8080 -v $PWD:/app bigtruedata/sbt sbt tomcat:start tomcat:join
```

Test it out:

```
$ curl localhost:8080/hello
<html>
  <body>
    <h1>Hello, world!</h1>
  </body>
</html>
```

### Run a packaged *.war* file from Docker

First, package a *.war* file:

```
$ sbt package
[info] Packaging .../target/scala-2.10/getting-started_2.10-0.1-SNAPSHOT.war .
```

Run it using [Jetty](https://hub.docker.com/_/jetty/):

```
$ docker run -p 8080:8080 -v /path/to/myproject/target/scala-2.10:/var/lib/jetty/webapps jetty
```

Run it using [Tomcat](https://hub.docker.com/_/tomcat/):

```
$ docker run -p 8080:8080 -v /path/to/myproject/target/scala-2.10:/usr/local/tomcat/webapps tomcat
```

Test it out:

```
$ curl localhost:8080/myproject_2.10-0.1-SNAPSHOT/hello
<html>
  <body>
    <h1>Hello, world!</h1>
  </body>
</html>
```

### Quickstart mode

The development cycle can be sped up by serving static resources
directly from source, and avoiding packaging of compiled artifacts.

Use `<container>:quickstart` in place of `<container>:start` to run the
container in quickstart mode:

```
> jetty:quickstart
```

### Running multiple containers

To launch using more than a single container, set `containerScale`:

```scala
containerScale := 5
```

This will configure the container to launch in five forked JVMs, using
five sequential ports starting from `containerPort`.

In debug mode, five additional sequential debug ports starting from
`debugPort` will be opened.

### JRebel integration

The development cycle can be further sped up by skipping server restarts
between code recompilation.

Add `-agentpath` to the container's JVM options:

```
javaOptions in Jetty += "-agentpath:/path/to/jrebel/lib/libjrebel64.so"
```

Launch the container with `quickstart`, and run triggered compilation:

```
> jetty:quickstart
> ~compile
```
