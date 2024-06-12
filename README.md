# SCIM 2.0 Compliance Test Suite

This is a fork of [wso2-incubator/scim2-compliance-test-suite](https://github.com/wso2-incubator/scim2-compliance-test-suite/tree/test-suite-v2) that aims to fix and improve the library for our use in the Nuxeo SCIM 2.0 [compliance tests](https://github.com/nuxeo/nuxeo-lts/blob/2023/modules/platform/nuxeo-scim-v2/src/test/java/org/nuxeo/scim/v2/tests/compliance/ScimV2ComplianceTest.java).

Original readme can be found [there](README.ori.md).

## Use the Library

You can download this library from our [artifactory](https://packages.nuxeo.com/#browse/search/maven=attributes.maven2.groupId%3Dorg.wso2.scim2.testsuite).

Or, with Maven:

```xml
<project>
  ...
  <dependencyManagement>
    <dependencies>
      <dependency>
        <groupId>org.wso2.scim2.testsuite</groupId>
        <artifactId>scim2-compliance-test-suite</artifactId>
        <version>VERSION</version>
        <type>pom</type>
        <scope>import</scope>
      </dependency>
    </dependencies>
  </dependencyManagement>

  ...

  <repositories>
    <repository>
      <id>nuxeo-public</id>
      <url>https://packages.nuxeo.com/repository/maven-public/</url>
      <releases>
        <enabled>true</enabled>
      </releases>
      <snapshots>
        <updatePolicy>always</updatePolicy>
        <enabled>true</enabled>
      </snapshots>
    </repository>
  </repositories>
  ...
</project>
```

## Release the Projet

Make sure the project builds and its tests pass.

Then, create a temporary branch to perform the release:

```bash
git checkout -b tmp-release
```

Then, update the project version to the release one, for instance 1.1-NX02:

```bash
mvn versions:set -DnewVersion=1.1-NX02 -DgenerateBackupPoms=false
```

Then, commit and tag the release:

```bash
git commit -a -m "Release 1.1-NX02"
git tag -a -m "Release 1.1-NX02" 1.1-NX02
```

Then, deploy the Maven artefacts:

```bash
mvn clean deploy -DskipTests -DaltDeploymentRepository=maven-vendor::default::VENDOR_URL
```

> [!IMPORTANT]
> You should replace the `VENDOR_URL`.
> Your Maven `settings.xml` file should contain appropriate authentication (if any) for the `maven-vendor` repository.

Then, push the tag:

```bash
git push --tags
```

Finally, clean up your branch and prepare the next development iteration:

```bash
git checkout test-suite-v2
git branch -D tmp-release
mvn versions:set -DnewVersion=1.1-NX03-SNAPSHOT -DgenerateBackupPoms=false
git commit -a -m "Post release 1.1-NX02"
git push
```
