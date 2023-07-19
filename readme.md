SupplyOn RichFaces 3.2.1.GA Fork
================================

# Resources 

| Name                      | Description                                       |
|---------------------------|---------------------------------------------------|
| RichFaces Repository      | https://repository.jboss.org/maven2/org/richfaces |
| RichFaces 3.3 Nuxeo Fork  | https://github.com/nuxeo/richfaces-3.3            |
| SupplyOn RichFaces Fixes  | https://doc.supplyon.de/x/woWZBg                  |
 

# Prerequisites

1. Download and Install maven: `apache-maven-3.9.3` from: [Apache Distribution Directory](https://dlcdn.apache.org/maven/maven-3/3.9.3/binaries/apache-maven-3.9.3-bin.zip)
 
2. Download and Install `jdk1.5.0_12` from: [Oracle Archive Download](https://www.oracle.com/java/technologies/java-archive-javase5-downloads.html)  
 Also archive could be found at: `Project Shared Directory`: `\\epam.com\Projects\Minsk\SLON-INIT\Software\java\jdk-1_5_0_12-windows-i586-p.exe`
 
3. Copy `toolchnains.xml` to `~/m2./toolchains.xml` 
```cmd
copy toolchnains.xml ~/m2/toolchains.xml
```

4. Copy `settings.xml` to `~/m2/settings.xml` and set `password` to correct value for `epam-repository`
```cmd
copy settings.xml ~/m2/settings.xml
```
 

# Build project

```cmd
mvn clean package
```

# Publish project

- Publish project to [epam-repository](https://jenkins.slon-srm.projects.epam.com/artifactory/SLON-SRM)   
  Note: check that username/password was defined for `epam-repository` in `~/m2/settings.xml`   
```cmd
mvn deploy
```

- Copy generated artifacts to permanent storage according to [Adding new libraries to Artifactory](https://doc.supplyon.de/x/_YiZBg).
  Path in Bit Bucket repository: [RichFaces artifacts](https://bitbucket.supplyon.de/projects/EXT_SRMPORTAL/repos/supplyon-gradle-utils/browse/upload-artifacts/supplyon-artifacts/repository/com/supplyon/richfaces/richfaces-impl)

# Notes
- Java `1.5.0_12` is used to build the project 
  `framework/impl/pom.xml`
  ```
      <plugin>
        <groupId>org.apache.maven.plugins</groupId>
        <artifactId>maven-toolchains-plugin</artifactId>
        <version>3.1.0</version>
        <executions>
          <execution>
            <goals>
              <goal>toolchain</goal>
            </goals>
          </execution>
        </executions>
        <configuration>
          <toolchains>
            <jdk>
              <version>1.5</version>
              <vendor>sun</vendor>
            </jdk>
          </toolchains>
        </configuration>
      </plugin>
  ```
 
- `scriptaculous.js` is assembled as concat of files: `builder.js,effects.js,dragdrop.js,controls.js,slider.js,sound.js` 
  using Apache Ant script: `generatescript.xml`
  ```
        <concat append="false"
            binary="false"
            destfile="${target.dir}/${richfaces.scripts.path}/scriptaculous/scriptaculous.js"
            fixlastline="yes"
            eol="unix">
            <filelist refid="scriptaculous"></filelist>
        </concat>
  ``` 
  
- JavaScript files are copied to `target` directory using Apache Ant script `generatescript.xml`
  ```
        <copy todir="${target.dir}/${ajax4jsf.scripts.path}">
            <fileset dir="${source.dir}/${ajax4jsf.scripts.path}" includes="**/*.js"/>
        </copy>
        <copy todir="${target.dir}/${richfaces.scripts.path}">
            <fileset dir="${source.dir}/${richfaces.scripts.path}" includes="**/*.js"/>
        </copy>
  ```
   
- JavaScript files are compressed and concat into `framework.pack.js` file using `org.richfaces.cdk:maven-javascript-plugin` plugin
  `framework/impl/pom.xml`
  ```
      <plugin>
        <groupId>org.richfaces.cdk</groupId>
        <artifactId>maven-javascript-plugin</artifactId>
        <version>3.2.1.GA</version>
        <dependencies>
          <dependency>
            <groupId>org.codehaus.plexus</groupId>
            <artifactId>plexus-utils</artifactId>
            <version>1.4.9</version>
          </dependency>
        </dependencies>
      </plugin>
  ``` 
 