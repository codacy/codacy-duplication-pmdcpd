[![Codacy Badge](https://api.codacy.com/project/badge/grade/d0f04e048bba4a01a7ef0166bf5b8d32)](https://www.codacy.com/app/Codacy/codacy-duplication-pmdcpd)
[![Build Status](https://circleci.com/gh/codacy/codacy-duplication-pmdcpd.svg?style=shield&circle-token=:circle-token)](https://circleci.com/gh/codacy/codacy-duplication-pmdcpd)
[![Docker Version](https://images.microbadger.com/badges/version/codacy/codacy-duplication-pmdcpd.svg)](https://microbadger.com/images/codacy/codacy-duplication-pmdcpd "Get your own version badge on microbadger.com")

# Codacy Duplication PMD CPD

This is the duplication docker we use at Codacy to have [PMD CPD](https://pmd.github.io/) support.
You can also create a docker to integrate the tool and language of your choice!
             
## Usage

You can create the docker by doing:

```bash
./scripts/publish.sh
```

The docker is ran with the following command:

```bash
docker run -it -v $srcDir:/src  <DOCKER_NAME>:<DOCKER_VERSION>
docker run -it -v $PWD/src/test/resources:/src codacy/codacy-duplication-pmdcpd:latest
```

## Test

```bash
./scripts/test.sh
```


## What is Codacy?

[Codacy](https://www.codacy.com/) is an Automated Code Review Tool that monitors your technical debt, helps you improve your code quality, teaches best practices to your developers, and helps you save time in Code Reviews.

### Among Codacy’s features:

- Identify new Static Analysis issues
- Commit and Pull Request Analysis with GitHub, BitBucket/Stash, GitLab (and also direct git repositories)
- Auto-comments on Commits and Pull Requests
- Integrations with Slack, HipChat, Jira, YouTrack
- Track issues in Code Style, Security, Error Proneness, Performance, Unused Code and other categories

Codacy also helps keep track of Code Coverage, Code Duplication, and Code Complexity.

Codacy supports PHP, Python, Ruby, Java, JavaScript, and Scala, among others.

### Free for Open Source

Codacy is free for Open Source projects.

