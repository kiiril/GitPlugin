package com.github.kiiril.gitplugin.services;

import com.intellij.openapi.components.Service;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.roots.libraries.Library;
import com.intellij.openapi.roots.libraries.LibraryTablesRegistrar;
import com.intellij.openapi.vcs.VcsException;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.vcs.log.VcsUser;
import git4idea.GitCommit;
import git4idea.GitUserRegistry;
import git4idea.history.GitHistoryUtils;
import git4idea.repo.GitRepository;
import git4idea.repo.GitRepositoryManager;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// THIS CLASS WAS USED AS A BASIS FOR KOTLIN CLASS
@Service(Service.Level.PROJECT)
public final class ProfileServiceJava {
    private final Project project;

    public ProfileServiceJava(Project project) {
        this.project = project;
    }

    public List<GitCommit> getCommits() {
        GitRepository repository = GitRepositoryManager.getInstance(project).getRepositories().get(0);
        try {
            return GitHistoryUtils.history(project, repository.getRoot());
        } catch (VcsException e) {
            System.out.println("Problem with running git: " + e.getMessage());
        }
        return Collections.emptyList();
    }

    public String[][] getCommitsForTable() {
        List<GitCommit> commits = getCommits();
        int size = commits.size();
        String[][] commitsData = new String[size][size];
        for (int i = 0; i < size; i++) {
            GitCommit commit = commits.get(i);
            Instant instant = Instant.ofEpochMilli(commit.getTimestamp());
            LocalDateTime localDateTime =
                    LocalDateTime.ofInstant(instant, ZoneId.of("UTC"));

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yy HH:mm");
            String formattedDateTime = localDateTime.format(formatter);
            commitsData[i] = new String[] {formattedDateTime, commit.getFullMessage(), commit.getAuthor().getName()};
        }
        return commitsData;
    }

    public String[] getUserAndEmail() {
        VcsUser user = GitUserRegistry.getInstance(project).getUser(GitRepositoryManager.getInstance(project).getRepositories().get(0).getRoot());
        return new String[]{user.getName(), user.getEmail()};
    }

    public Map<String, Double> getTechnologies() {
        Map<String, Integer> technologyCounts = new HashMap<>();
        traverseProject(project, technologyCounts);
        // Calculate the total number of files
        int totalFiles = technologyCounts.values().stream().mapToInt(Integer::intValue).sum();

        // Calculate the percentages for each technology
        Map<String, Double> technologyPercentages = new HashMap<>();
        for (Map.Entry<String, Integer> entry : technologyCounts.entrySet()) {
            String technology = entry.getKey();
            int count = entry.getValue();
            double percentage = (double) count / totalFiles * 100;
            technologyPercentages.put(technology, percentage);
        }
        return technologyPercentages;
    }

    public String[] getProjectLibraries() {
        Library[] libraries = LibraryTablesRegistrar.getInstance().getLibraryTable(project).getLibraries();
        String[] libs = new String[libraries.length];
        for (int i = 0; i < libs.length; i++) {
            libs[i] = libraries[i].getName();
        }
        return libs;
    }

    private void traverseProject(Project project, Map<String, Integer> technologyCounts) {
        VirtualFile[] rootDirs = ProjectRootManager.getInstance(project).getContentSourceRoots();

        for (VirtualFile rootDir : rootDirs) {
            traverseDirectory(rootDir, technologyCounts);
        }
    }

    private void traverseDirectory(VirtualFile dir, Map<String, Integer> technologyCounts) {
        for (VirtualFile file : dir.getChildren()) {
            if (file.isDirectory()) {
                traverseDirectory(file, technologyCounts);
            } else {
                String extension = file.getExtension();
                if (extension != null) {
                    String technology = getTechnologyFromExtension(extension);
                    technologyCounts.merge(technology, 1, Integer::sum);
                }
            }
        }
    }

    private String getTechnologyFromExtension(String extension) {
        switch (extension) {
            case "java": return "Java";
            case "py": return "Python";
            case "js": return "JavaScript";
            case "css": return "Css";
            case "kt": return "Kotlin";
            default: return "Unknown";
        }
    }
}
