[![Build Status](https://travis-ci.org/earldouglas/xsbt-web-plugin.png?branch=master)](https://travis-ci.org/earldouglas/xsbt-web-plugin)

xsbt-web-plugin is an extension to [sbt](http://www.scala-sbt.org/) for building enterprise Web applications based on the [Java J2EE Servlet specification](http://en.wikipedia.org/wiki/Java_Servlet).

xsbt-web-plugin supports both Scala and Java, and is best suited for projects that:

* Deploy to common cloud platforms (e.g. [Google App Engine](https://developers.google.com/appengine/), [Heroku](https://www.heroku.com/), [Elastic Beanstalk](https://console.aws.amazon.com/elasticbeanstalk/home), [Jelastic](http://jelastic.com/))
* Deploy to production J2EE environments (e.g. Tomcat, Jetty, GlassFish, WebSphere)
* Incorporate J2EE libraries (e.g. [JSP](http://en.wikipedia.org/wiki/JavaServer_Pages), [JSF](http://en.wikipedia.org/wiki/JavaServer_Faces), [EJB](http://en.wikipedia.org/wiki/Ejb))
* Utilize J2EE technologies (e.g. [`Servlet`](http://docs.oracle.com/javaee/6/api/javax/servlet/Servlet.html)s, [`Filter`](http://docs.oracle.com/javaee/6/api/javax/servlet/Filter.html)s, [JNDI](http://en.wikipedia.org/wiki/Java_Naming_and_Directory_Interface))
* Have a specific need to be packaged as a [*.war* file](https://en.wikipedia.org/wiki/WAR_%28Sun_file_format%29)

## Requirements

* sbt 0.13.x
* Scala 2.10.x

## Getting started 

The quickest way to get started is to clone the [xwp-template](https://github.com/earldouglas/xwp-template) project, which sets up the necessary directories, files, and configuration for a basic xsbt-web-plugin project.

For more information, please see the [wiki](http://github.com/earldouglas/xsbt-web-plugin/wiki/).

## Quick reference

First, add xsbt-web-plugin:

*project/plugins.sbt*:

```scala
addSbtPlugin("com.earldouglas" % "xsbt-web-plugin" % "1.0.0-M1")
```

Then choose either Jetty or Tomcat with default setings:

*build.sbt*:

```scala
jetty()
```

*build.sbt*:

```scala
tomcat()
```

Start (or restart) the container with `container:start`:

*sbt console:*

```
> container:start
```

Stop the container with `container:stop`:

*sbt console:*

```
> container:stop
```

Build a *.war* file with `package`:

*sbt console:*

```
> package
```

## Configuration and usage

**Triggered (re)launch**

*sbt console:*

```
> ~container:start
```

**Configure Jetty to run on port 9090**

*build.sbt:*

```scala
jetty(port = 9090)
```

**Configure Tomcat to run on port 9090**

*build.sbt:*

```scala
tomcat(port = 9090)
```

**Configure Jetty with jetty.xml**

*build.sbt:*

```scala
jetty(config = "etc/jetty.xml")
```

**Depend on projects in a multi-project build**

*build.sbt:*

```scala
lazy val root = (project in file(".")) aggregate(mylib1, mylib2, mywebapp)

lazy val mylib1 = project

lazy val mylib2 = project

lazy val mywebapp = project webappDependsOn (mylib1, mylib2)
```

Here we use `webappDependsOn` in place of the usual `dependsOn` function (which 
will be called automatically).

**Add an additional source directory**

Scala files in a source directory are compiled, and bundled in the project 
artifact *.jar* file.

*build.sbt:*

```scala
// add <project>/src/main/extra as an additional source directory
unmanagedSourceDirectories in Compile <+= (sourceDirectory in Compile)(_ / "extra")
```

**Add an additional resource directory**

Files in a resource directory are not compiled, and are bundled directly in the 
project artifact *.jar* file.

*build.sbt:*

```scala
// add <project>/src/main/extra as an additional resource directory
unmanagedResourceDirectories in Compile <+= (sourceDirectory in Compile)(_ / "extra")
```

**Change the default Web application resources directory**

The Web application resources directory is where static Web content (including 
*.html*, *.css*, and *.js* files, the *web.xml* container configuration file, 
etc.  By default, this is kept in *<project>/src/main/webapp*.

*build.sbt:*

```scala
// set <project>/src/main/WebContent as the webapp resources directory
webappSrc in webapp <<= (sourceDirectory in Compile) map  { _ / "WebContent" }
```

**Change the default Web application destination directory**

The Web application destination directory is where the static Web content, 
compiled Scala classes, library *.jar* files, etc. are placed.  By default, 
they go to *<project>/target/webapp*.

*build.sbt:*

```scala
// set <project>/target/WebContent as the webapp destination directory
webappDest in webapp <<= target map  { _ / "WebContent" }
```

**Modify the contents of the prepared Web application**

After the *<project>/target/webapp* directory is prepared, it can be modified 
with an arbitrary `File => Unit` function.

*project/plugins.sbt*:

```scala
libraryDependencies += "com.yahoo.platform.yui" % "yuicompressor" % "2.4.7" intransitive()
```

*build.sbt:*

```scala
// minify the JavaScript file script.js to script-min.js
postProcess in webapp := {
  webappDir =>
    import java.io.File
    import com.yahoo.platform.yui.compressor.YUICompressor
    val src  = new File(webappDir, "script.js")
    val dest = new File(webappDir, "script-min.js")
    YUICompressor.main(Array(src.getPath, "-o", dest.getPath))
}
```

**Use *WEB-INF/classes* instead of *WEB-INF/lib***

By default, project classes and resources are packaged in the default *.jar* 
file artifact, which is copied to *WEB-INF/lib*.  This file can optionally be 
ignored, and the project classes and resources copied directly to 
*WEB-INF/classes*.

*build.sbt:*

```scala
webInfClasses in webapp := true
```

**Prepare the Web application for execution and deployment**

For situations when the prepared *<project>/target/webapp* directory is needed, 
but the packaged *.war* file isn't.

*sbt console:*

```
webapp:prepare
```

**Use a cusom webapp runner**

By default, either Jetty's [jetty-runner](http://wiki.eclipse.org/Jetty/Howto/Using_Jetty_Runner) 
or Tomcat's [webapp-runner](https://github.com/jsimone/webapp-runner) will be 
used to launch the container under `container:start`.

To use a custom runner, use `runnerContainer` with `warSettings` and 
`webappSettings`:

*build.sbt:*

```scala
runnerContainer(
  libs = Seq(
      "org.eclipse.jetty" %  "jetty-webapp" % "9.1.0.v20131115" % "container"
    , "org.eclipse.jetty" %  "jetty-plus"   % "9.1.0.v20131115" % "container"
    , "test"              %% "runner"       % "0.1.0-SNAPSHOT"  % "container"
  ),
  args = Seq("runner.Run", "8080")
) ++ warSettings ++ webappSettings
```

Here, `libs` includes the `ModuleID`s of libraries needed to make our runner, 
which is invoked by calling the main method of `runner.Run` with a single 
argument to specify the server port.
