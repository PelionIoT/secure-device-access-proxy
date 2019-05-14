## What is mgit? ##

mgit (Multiple Git) allows working with multiple git repositories in a
single directory structure.

mgit tries to do one thing, and keep things simple.

### Configuration File ###

mgit.yaml is the mgit configuration file, and describes the desired
tree structure.

Its basic structure is -

```
repos:

  - url: git@github.com:ARMmbed/factory-client
    revision: 746f14598c726bb496636e39eb3ed61de2658c2e
    target-dir: factory-client

  - url: git@github.com:ARMmbed/e2e-common-utils
    revision: master
    target-dir: e2e-common-utils
```

### Commands ###

* `mgit sync` synchronizes the repositories in the configuration
  file with the current directory.
* `mgit status` shows status of each of the repositories.

#### Synchronizig ####

To synchronize an mgit configuration file with the current directory -

```
mgit sync
```

Each repository mentioned in the configuration file is cloned
to its respective target directory, and the configured revision
is checked-out (it can be a branch name, a tag name or
a specific commit hash).

If the repository is already cloned, only a fetch and checkout
are performed.

If changes were previously made to the checked-out repository,
a new checkout may fail. In this case, mgit immediately stops,
to prevent loss of information, and manual steps need to be taken
(for example, revert or commit the changes made).

If the configured commit target is a branch HEAD, it is possible
to work with the sub-repository normally, making changes to it,
committing, pushing, pulling etc.

If the target is not a branch HEAD (for example, it is a specific
commit hash), it is checked-out in detached HEAD mode. In this
case, to avoid losing changes, it is recommended to create a branch
or checkout to a branch HEAD before making changes.

### Alternatives and Similar Tools ###

Various other tools provide functionliaty similar to mgit -

 * git submodule - https://git-scm.com/docs/git-submodule
 * git subtree - http://git-memo.readthedocs.io/en/latest/subtree.html
 * repo - https://code.google.com/p/git-repo/
 * gclient - https://pypi.python.org/pypi/gclient
 * yotta - https://github.com/ARMmbed/yotta
 * mbed CLI - https://github.com/ARMmbed/mbed-cli
 * peru - https://github.com/buildinspace/peru

They all either do too much (combine repository management with a build
system) or too little (not preserving the git repository).
