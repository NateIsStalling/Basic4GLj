# Contributing to Basic4GLj

Thanks for your interest in contributing to Basic4GLj!

Contributions are welcome, whether you're fixing a bug, adding tests, improving documentation, or helping improve the editor and runtime.

## Before You Start

For small and focused changes, feel free to open a pull request directly. You do not need to create an issue first for things like:

* Bug fixes
* Regression fixes
* Unit tests
* Documentation improvements
* Small refactors or cleanup with no intended behavior change

For larger changes, opening an issue or discussion first is encouraged. This is especially helpful for changes involving:

* Compiler or virtual machine behavior
* Language semantics or compatibility
* Significant editor or UI changes
* Plugin or SPI API changes
* New subsystems or major architectural changes
* Changes to the standard library

Discussing larger changes ahead of time helps make sure the approach fits the direction of the project before significant work is invested.

## Library Functions and Plugins

Basic4GLj aims to keep the core language and standard library compatible with Basic4GL where practical.

New functionality does not necessarily need to become part of the standard library. New library functions and optional capabilities are generally encouraged to use the plugin SDK instead.

Changes to existing standard-library functionality are still welcome when fixing bugs, improving compatibility, or addressing existing behavior.

If you're unsure whether something belongs in the standard library or a plugin, feel free to open an issue or discussion.

## Development Setup

Basic4GLj requires Java 17 and uses Gradle.

To build the project:

```sh
./gradlew build
```

On Windows:

```sh
gradlew.bat build
```

## Making Changes

When contributing:

* Keep changes focused on the problem being addressed.
* Follow the conventions of the surrounding code.
* Avoid unrelated formatting or refactoring in the same pull request.
* Add or update tests when fixing bugs or changing behavior where practical.
* Consider compatibility with existing Basic4GL programs when modifying compiler, runtime, or library behavior.
* Avoid introducing breaking changes to plugin APIs without prior discussion.

A smaller, focused pull request is generally easier to review than one containing several unrelated changes.

## Pull Requests

When opening a pull request, please include:

* A short description of the problem or goal.
* An explanation of the approach taken.
* How the change was tested.
* Any known compatibility concerns or limitations.

For bug fixes, including a regression test is especially helpful when the issue can be reproduced reliably.

Before submitting or updating a pull request, please run:

```sh
./gradlew build
```

On Windows:

```sh
gradlew.bat build
```

Basic4GLj uses Spotless for code formatting. Running the build applies the project's formatting rules and runs the project's build checks. Please make sure the build completes successfully before submitting your changes.

Draft pull requests are also welcome if you'd like feedback on an approach before the implementation is complete.

Pull requests may receive requests for changes or discussion before being merged. Not every proposed feature will necessarily fit the project's direction, even if the implementation itself is sound.

## Reporting Bugs

Bug reports are welcome through GitHub Issues.

When possible, include:

* The Basic4GLj version or commit being used.
* Your operating system.
* Steps to reproduce the problem.
* A small example program that demonstrates the issue.
* The expected behavior.
* The actual behavior, including any error messages.

Compatibility differences between Basic4GLj and the original Windows releases of Basic4GL are also useful to report.

## Licensing

Basic4GLj is licensed under the BSD 3-Clause License.

By submitting a contribution to the project, you agree that your contribution may be distributed under the same license.

## Questions

If you're unsure whether a change is in scope or how best to implement it, opening an issue or discussion before starting is welcome.

Thanks for helping improve Basic4GLj.
