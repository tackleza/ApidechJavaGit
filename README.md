# ApidechJavaGit

A pure-Java Git client library built on JGit for common Git operations with optional authentication.

## Features

* Clone repositories using `File` target directory (with optional username/password credentials)
* Open existing repositories via `File repoDir` constructor
* Branch management: `createBranch`, `checkoutBranch`, `getCurrentBranch`
* Staging and commits: `addAll`, `commit`
* History operations: `fetch`, `pull`, `push`
* Workspace cleanup: `reset(ResetType)`, `resetHard`, `clean`
* Tagging: `tag`, `tagAnnotated`, `listTags`, `deleteTag`, `pushTag`

## Prerequisites

* Java 21 or newer
* Maven 3.8+

## Installation

Add the dependency to your project’s `pom.xml`:

```xml
<dependency>
  <groupId>com.apidech.lib</groupId>
  <artifactId>apidechjavagit</artifactId>
  <version>0.0.1-SNAPSHOT</version>
</dependency>
```

## Usage

### Cloning & Working with a New Repository

```java
import com.example.gitlib.GitClient;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.ResetCommand.ResetType;
import java.io.File;
import java.io.IOException;

public class Example {
    public static void main(String[] args) {
        File repoDir = new File("/path/to/local/repo");
        try {
            // Clone a repository with credentials
            GitClient git = GitClient.cloneRepository(
                "https://github.com/your-org/private-repo.git",
                repoDir,
                "username",
                "passwordOrToken"
            );

            // Branch operations
            String current = git.getCurrentBranch();
            git.createBranch("feature-x");
            git.checkoutBranch("feature-x");

            // Stage & commit
            git.addAll();
            git.commit("Implement feature X");

            // Tagging
            git.tagAnnotated("v1.0.0", "Release version 1.0.0");
            git.pushTag("v1.0.0");

            // Remote sync
            git.fetch();
            git.pull();
            git.push();

            // Cleanup
            git.reset(ResetType.HARD);
            git.clean();

            git.close();
        } catch (IOException | GitAPIException e) {
            e.printStackTrace();
        }
    }
}
```

### Opening & Using an Existing Repository

```java
import com.example.gitlib.GitClient;
import org.eclipse.jgit.api.errors.GitAPIException;
import java.io.File;
import java.io.IOException;

public class ExistingExample {
    public static void main(String[] args) {
        File repoDir = new File("/path/to/local/repo");
        try {
            GitClient client = new GitClient(
                repoDir,
                "username",
                "passwordOrToken"
            );
            // List tags
            client.listTags().forEach(ref -> System.out.println(ref.getName()));

            // Delete a tag locally
            client.deleteTag("old-tag");

            // Pull updates
            client.pull();
            client.close();
        } catch (IOException | GitAPIException e) {
            e.printStackTrace();
        }
    }
}
```
