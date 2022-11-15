# FanConnie AI Data Gatherer

This platform facilitates the construction of expansive datasets that can be utilized for empirical software engineering studies, or in the training of Deep Learning models designed to automate various software engineering tasks.

## Modules

Several modules encompass this project:

- `ai-model`: A domain-specific module mapping relational database structures to the programming environment.
- `ai-analyzer`: Contains code analysis operations anchored on `tree-sitter`.
- `ai-transformer`: Hosts code transformation operations based on `tree-sitter`.
- `ai-crawler`: A standalone application used for mining source code from GitHub repositories via [GitHub Search](https://seart-ghs.si.usi.ch/).
- `ai-server`: A Spring Boot server application that serves as the platform back-end piece.
- `ai-spring`: Common Spring Boot configurations and utilities used in the server and crawler.
- `ai-website`: A front-end web application designed in Vue.

## How To Install and Use

This section explains how to set up and run the project in your local environment.

### [Environment](README_ENV.md)

### [Database](README_DB.md)

### [Usage](README_RUN.md)

### [Dockerization](README_DOCKER.md)

## License

[MIT](LICENSE)

## Frequently Asked Questions

### How do you implement language-specific analysis heuristics?

Heuristics for identifying test code in Java and Python can be accessed [here](ai-analyzer/src/main/java/ch/usi/si/seart/analyzer/predicate/path/JavaTestFilePredicate.java) and [here](ai-analyzer/src/main/java/ch/usi/si/seart/analyzer/predicate/path/PythonTestFilePredicate.java). Boilerplate code identification heuristics can be accessed [here](ai-analyzer/src/main/java/ch/usi/si/seart/analyzer/enumerate/JavaBoilerplateEnumerator.java) and [here](ai-analyzer/src/main/java/ch/usi/si/seart/analyzer/enumerate/PythonBoilerplateEnumerator.java) respectively.

### How can I request a feature or ask a question?

If you have any feature proposals or questions, we encourage initiating a [new discussion](https://github.com/fanconnie/Software-Engineering-AI-Dataset-Tool/discussions). Through this, you can connect with the community and our team.

### How can I report bugs?

To report bugs or issues, create a [new issue](https://github.com/fanconnie/Software-Engineering-AI-Dataset-Tool/issues/). The more detailed your issue report, the more effectively we can acknowledge and address it.