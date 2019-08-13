# ----------------------------------------------------------------------------
# Copyright 2017-2019 ARM Ltd.
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
# ----------------------------------------------------------------------------

# noinspection PyUnresolvedReferences
import vendor_import_packages

import subprocess
import os
import yaml
import click
import sys

CONFIG_FILE = "mgit.yaml"
GIT_EXECUTABLE = "git"

config = None


def read_config():
    # noinspection PyBroadException
    try:
        with open(CONFIG_FILE, "rt") as yaml_file:
            return yaml.load(yaml_file)
    except:
        print("Error reading configuration file '" + CONFIG_FILE + "'.")
        sys.exit(1)


def git_set_config(username, mail):

    # git config user.name "user name"
    subprocess.check_call(
        [
            GIT_EXECUTABLE,
            "config",
            "user.name",
            username
        ]
    )

    # git config user.email "user name"
    subprocess.check_call(
        [
            GIT_EXECUTABLE,
            "config",
            "user.email",
            mail
        ]
    )


def git_repo_exists(directory):

    if not os.path.exists(directory):
        return False

    process = subprocess.Popen(
        [
            GIT_EXECUTABLE,
            "status"
        ],
        cwd=directory,
        stdout=subprocess.PIPE,
        stderr=subprocess.STDOUT
    )
    process.communicate()

    if process.returncode == 0:
        return True
    else:
        return False


def git_clone(repository, target_dir):

    ret = subprocess.check_call(
        [
            GIT_EXECUTABLE,
            "clone",
            repository,
            target_dir,
            "--progress"
        ]
    )
    if ret != 0:
        return False
    else:
        return True


def git_add_all_files():

    # git add *
    subprocess.check_call(
        [
            GIT_EXECUTABLE,
            "add",
            "*"
        ]
    )


def git_add_file(full_path_filename):

    # git add *
    subprocess.check_call(
        [
            GIT_EXECUTABLE,
            "add",
            full_path_filename
        ]
    )


def git_commit(commit_message):

    # git commit -m "Commit message"
    subprocess.check_call(
        [
            GIT_EXECUTABLE,
            "commit",
            "-m",
            commit_message
        ]
    )


def git_push(repository):

    # git push origin master
    subprocess.check_call(
        [
            GIT_EXECUTABLE,
            "push",
            "origin",
            repository
        ]
    )


def git_show_head_hash(directory):

    print("HEAD HASH IS:")
    subprocess.check_call(
        [
            GIT_EXECUTABLE,
            "rev-parse",
            "HEAD",
        ],
        cwd=directory
    )


def git_fetch(directory):
    ret = subprocess.check_call(
        [
            GIT_EXECUTABLE,
            "fetch",
            "--progress"
        ],
        cwd=directory
    )
    if ret != 0:
        return False
    else:
        return True


def git_checkout(directory, revision):
    process = subprocess.Popen(
        [
            GIT_EXECUTABLE,
            "checkout",
            revision
        ],
        cwd=directory,
        stdout=subprocess.PIPE,
        stderr=subprocess.STDOUT
    )
    (output, error) = process.communicate()

    if process.returncode != 0:
        print (output)
        return False
    else:
        return True


def git_sync(repository, revision, target_dir):

    if not git_repo_exists(target_dir):
        # Clone.
        print ("Cloning repository.")
        clone_success = git_clone(repository, target_dir)
        if not clone_success:
            return False
    else:
        # Fetch.
        print("Updating repository.")
        fetch_success = git_fetch(target_dir)
        if not fetch_success:
            return False

    # Checkout.
    if revision != "":
        print("Checking out '" + revision + "'.")
        checkout_success = git_checkout(target_dir, revision)
        if not checkout_success:
            print ("Checkout failed. You probably have pending files. Commit or revert them first.")
            return False

    git_show_head_hash(target_dir)

    return True


def git_pending_changes(directory):

    if not os.path.exists(directory):
        print ("--> Directory not found!")
        return
    output = subprocess.check_output(
        [
            GIT_EXECUTABLE,
            "status",
            "--porcelain"
        ],
        cwd=directory
    )
    if output != "":
        print ("--> Changes exist!")
    else:
        print ("--> No pending changes")


@click.group()
def cli():
    pass


@cli.command()
def sync():

    repos = config["repos"]
    for repo in repos:
        print("==> Synchronizing repository " + repo["url"] + " into " + repo["target-dir"] + ".")
        success = git_sync(repo["url"], repo["revision"], repo["target-dir"])
        if not success:
            sys.exit(1)


@cli.command()
def status():

    repos = config["repos"]
    for repo in repos:
        print(repo["target-dir"] + " [" + repo["url"] + "]")
        git_pending_changes(repo["target-dir"])


if __name__ == "__main__":
    config = read_config()
    cli()
