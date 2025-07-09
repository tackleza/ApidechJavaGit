package com.apidech.lib.apidechjavagit;

import java.io.File;
import java.io.IOException;

import org.eclipse.jgit.api.CleanCommand;
import org.eclipse.jgit.api.CloneCommand;
import org.eclipse.jgit.api.FetchCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.PullCommand;
import org.eclipse.jgit.api.PushCommand;
import org.eclipse.jgit.api.ResetCommand.ResetType;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.eclipse.jgit.transport.CredentialsProvider;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;

/**
 * A high-level Java wrapper for common Git operations, built on top of JGit.
 * Eliminates the need to manually manage ProcessBuilder for calling Git commands.
 *
 * <p>This library leverages the pure-Java implementation provided by
 * JGit (https://www.eclipse.org/jgit/), so no native Git binary installation
 * is required on the host machine.
 */
public class ApidechJavaGit {
	
	private final Git git;
    private final CredentialsProvider credentialsProvider;

    /**
     * Opens an existing Git repository located at the given directory without authentication.
     *
     * @param repoPath the filesystem path of the existing Git repository
     * @throws IOException if repository cannot be opened
     */
    public ApidechJavaGit(String repoPath) throws IOException {
        this(repoPath, null, null);
    }

    /**
     * Opens an existing Git repository located at the given directory with authentication.
     *
     * @param repoPath   the filesystem path of the existing Git repository
     * @param username   the username for remote operations
     * @param password   the password (or token) for remote operations
     * @throws IOException if repository cannot be opened
     */
    public ApidechJavaGit(String repoPath, String username, String password) throws IOException {
        Repository repository = new FileRepositoryBuilder()
                .setGitDir(new File(repoPath, ".git"))
                .readEnvironment()
                .findGitDir()
                .build();
        this.git = new Git(repository);
        if (username != null && password != null) {
            this.credentialsProvider = new UsernamePasswordCredentialsProvider(username, password);
        } else {
            this.credentialsProvider = null;
        }
    }

    /**
     * Clones a Git repository from a remote URI into the specified local directory without authentication.
     *
     * @param remoteUri the URI of the remote repository (e.g. https://github.com/user/repo.git)
     * @param targetDir the local directory into which to clone
     * @return a GitClient instance pointing at the newly cloned repository
     * @throws GitAPIException if the clone operation fails
     */
    public static ApidechJavaGit cloneRepository(String remoteUri, File targetDir) throws GitAPIException {
        return cloneRepository(remoteUri, targetDir, null, null);
    }

    /**
     * Clones a Git repository from a remote URI into the specified local directory with authentication.
     *
     * @param remoteUri the URI of the remote repository
     * @param targetDir the local directory into which to clone
     * @param username  the username for remote operations
     * @param password  the password (or token) for remote operations
     * @return a GitClient instance pointing at the newly cloned repository
     * @throws GitAPIException if the clone operation fails
     */
    public static ApidechJavaGit cloneRepository(String remoteUri, File targetDir, String username, String password) throws GitAPIException {
        CredentialsProvider cp = null;
        if (username != null && password != null) {
            cp = new UsernamePasswordCredentialsProvider(username, password);
        }
        CloneCommand cmd = Git.cloneRepository()
                .setURI(remoteUri)
                .setDirectory(targetDir);
        if (cp != null) {
            cmd.setCredentialsProvider(cp);
        }
        Git git = cmd.call();
        return new ApidechJavaGit(git, cp);
    }

    // Internal constructor for wrapping an existing JGit Git instance
    private ApidechJavaGit(Git git, CredentialsProvider credentialsProvider) {
        this.git = git;
        this.credentialsProvider = credentialsProvider;
    }

    /**
     * Checks out the specified branch, creating it if it does not exist locally.
     *
     * @param branchName the name of the branch to checkout
     * @throws GitAPIException if the checkout operation fails
     */
    public void checkoutBranch(String branchName) throws GitAPIException {
        git.checkout()
           .setName(branchName)
           .setCreateBranch(false)
           .call();
    }

    /**
     * Creates a new branch pointing at the current HEAD.
     *
     * @param branchName the name of the new branch
     * @throws GitAPIException if branch creation fails
     */
    public void createBranch(String branchName) throws GitAPIException {
        git.branchCreate()
           .setName(branchName)
           .call();
    }

    /**
     * Retrieves the name of the current branch.
     *
     * @return the current branch name
     * @throws IOException if repository state cannot be read
     */
    public String getCurrentBranch() throws IOException {
        return git.getRepository().getBranch();
    }

    /**
     * Merges the specified branch into the current branch.
     *
     * @param sourceBranch the branch to merge from
     * @return the merge commit
     * @throws GitAPIException if the merge operation fails
     * @throws IOException 
     */
    public ObjectId mergeBranch(String sourceBranch) throws GitAPIException, IOException {
        return git.merge()
                .include(git.getRepository().findRef(sourceBranch))
                .call()
                .getNewHead();
    }

    /**
     * Adds all changes in the working directory to the index (staging area).
     *
     * @throws GitAPIException if the add operation fails
     */
    public void addAll() throws GitAPIException {
        git.add()
           .addFilepattern(".")
           .call();
    }

    /**
     * Commits staged changes with the provided message.
     *
     * @param message the commit message
     * @return the commit object
     * @throws GitAPIException if the commit operation fails
     */
    public RevCommit commit(String message) throws GitAPIException {
        return git.commit()
                .setMessage(message)
                .call();
    }

    /**
     * Performs a reset to the specified mode, e.g., HARD, MIXED, or SOFT.
     *
     * @param mode the reset mode from ResetType enum
     * @throws GitAPIException if the reset operation fails
     */
    public void reset(ResetType mode) throws GitAPIException {
        git.reset()
           .setMode(mode)
           .call();
    }

    /**
     * Convenience for a hard reset to the current HEAD, discarding all local changes.
     *
     * @throws GitAPIException if the reset operation fails
     */
    public void resetHard() throws GitAPIException {
        reset(ResetType.HARD);
    }

    /**
     * Removes untracked files and directories, cleaning the working tree.
     *
     * @throws GitAPIException if the clean operation fails
     */
    public void clean() throws GitAPIException {
        CleanCommand clean = git.clean();
        clean.setCleanDirectories(true)
             .setForce(true)
             .call();
    }

    /**
     * Pulls updates from the remote repository and merges into the current branch.
     *
     * @throws GitAPIException if the pull operation fails
     */
    public void pull() throws GitAPIException {
        PullCommand cmd = git.pull();
        if (credentialsProvider != null) {
            cmd.setCredentialsProvider(credentialsProvider);
        }
        cmd.call();
    }

    /**
     * Fetches updates from the remote repository without merging.
     *
     * @throws GitAPIException if the fetch operation fails
     */
    public void fetch() throws GitAPIException {
        FetchCommand cmd = git.fetch();
        if (credentialsProvider != null) {
            cmd.setCredentialsProvider(credentialsProvider);
        }
        cmd.call();
    }

    /**
     * Pushes commits from the current branch to its tracking remote.
     *
     * @throws GitAPIException if the push operation fails
     */
    public void push() throws GitAPIException {
        PushCommand cmd = git.push();
        if (credentialsProvider != null) {
            cmd.setCredentialsProvider(credentialsProvider);
        }
        cmd.call();
    }

    /**
     * Closes the Git repository and releases resources.
     */
    public void close() {
        git.close();
    }

    // Additional common actions (status, log, tag, etc.) can be added below with similar patterns.
}
