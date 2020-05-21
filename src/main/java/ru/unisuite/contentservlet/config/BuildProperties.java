package ru.unisuite.contentservlet.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class BuildProperties {
    private static final String GIT_PROPERTIES_FILENAME = "git.properties";
    private static final String GIT_PREFIX = "git.";

    private final String branch;
    private final String buildDate;
    private final String commitId;
    private final String commitDate;
    private final String shortMessage;
    private final String dirty;

    public BuildProperties() throws IOException {
        this(GIT_PROPERTIES_FILENAME);
    }

    public BuildProperties(String configFilePath) throws IOException {
        Properties prop = new Properties();
        try (InputStream input = this.getClass().getClassLoader().getResourceAsStream(configFilePath)) {
            prop.load(input);
        }

        this.branch = prop.getProperty(GIT_PREFIX + "branch");
        this.buildDate = prop.getProperty(GIT_PREFIX + "build.time");
        this.commitId = prop.getProperty(GIT_PREFIX + "commit.id");
        this.commitDate = prop.getProperty(GIT_PREFIX + "commit.time");
        this.shortMessage = prop.getProperty(GIT_PREFIX + "commit.message.short");
        this.dirty = prop.getProperty(GIT_PREFIX + "dirty");
    }

    public String getBranch() {
        return branch;
    }

    public String getBuildDate() {
        return buildDate;
    }

    public String getCommitId() {
        return commitId;
    }

    public String getCommitDate() {
        return commitDate;
    }

    public String getShortMessage() {
        return shortMessage;
    }

    public String getDirty() {
        return dirty;
    }

    // example
//    #Generated by Git-Commit-Id-Plugin
//    #Sun May 03 13:42:14 MSK 2020
//    git.branch=develop
//    git.build.host=vdi-chilikin
//    git.build.time=2020-05-03T13\:42\:14+0300
//    git.build.user.email=ilya.cyclone@gmail.com
//    git.build.user.name=ILya Cyclone
//    git.build.version=2.0.0-SNAPSHOT
//    git.closest.tag.commit.count=
//    git.closest.tag.name=
//    git.commit.id=eb31326767a12b16c1fc7392026572bb01bdf726
//    git.commit.id.abbrev=eb31326
//    git.commit.id.describe=eb31326-dirty
//    git.commit.id.describe-short=eb31326-dirty
//    git.commit.message.full=use image-processing facade
//    git.commit.message.short=use image-processing facade
//    git.commit.time=2020-05-03T05\:08\:03+0300
//    git.commit.user.email=ilya.cyclone@gmail.com
//    git.commit.user.name=ILya Cyclone
//    git.dirty=true
//    git.local.branch.ahead=2
//    git.local.branch.behind=0
//    git.remote.origin.url=https\://github.com/ILyaCyclone/ContentServlet
//    git.tags=
//    git.total.commit.count=141
}
