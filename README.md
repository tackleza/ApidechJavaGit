# ApidechJavaGit

A pure-Java Git client library built on JGit for common Git operations with optional authentication.

## Features

* Clone repositories (with optional username/password credentials)
* Open existing repositories
* Branch management: create, checkout, get current branch
* Staging and commits: add all, commit with message
* History operations: fetch, pull, push
* Workspace cleanup: hard reset, clean untracked files

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
            // Clone a private repository with credentials
            GitClient git = GitClient.cloneRepository(
                "https://github.com/your-org/private-repo.git",
                repoDir,
                "username",
                "passwordOrToken"
            );

            // Get current branch
            String branch = git.getCurrentBranch();
            System.out.println("On branch: " + branch);

            // Create and checkout a new feature branch
            git.createBranch("feature-x");
            git.checkoutBranch("feature-x");

            // Make changes, then add & commit
            // (modify files in repoDir...)
            git.addAll();
            git.commit("Add changes for feature X");

            // Push changes back to remote
            git.push();

            // Cleanup: hard reset and remove untracked files
            git.reset(ResetType.HARD);
            git.clean();

            git.close();
        } catch (IOException | GitAPIException e) {
            e.printStackTrace();
        }
    }
}
```

For opening an already-cloned repository:

```java
GitClient existing = new GitClient(
    "/path/to/local/repo",
    "username",
    "passwordOrToken"
);
existing.pull();
existing.close();
```
