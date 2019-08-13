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

# This package runs "pip install" in order to import all required packages into
# a local directory.
# This local directory is added to be a site directory, so the packages are
# accessible.

import os
import sys
import site
import platform

VENDOR_DIR_BASE = "vendor"

python_version = platform.python_version_tuple()

vendor_dir = VENDOR_DIR_BASE + "/" + platform.system() + \
                               "_" + platform.machine() + \
                               "_" + str(python_version[0]) + \
                               "_" + str(python_version[1]) + \
                               "_" + str(python_version[2])


script_dir = os.path.dirname(os.path.realpath(__file__))

# Install vendorized packages if they don't exist yet.
if not os.path.exists(script_dir + "/" + vendor_dir) or \
   (os.path.getmtime(script_dir + "/" + "requirements.txt") > os.path.getmtime(script_dir + "/" + vendor_dir)):
    import shutil
    try:
        shutil.rmtree(script_dir + "/" + vendor_dir)
    except:
        None
    sys.stderr.write('"' + vendor_dir + '" directory doesn\'t exist or not up-to-date. Installing all packages...\n')
    # noinspection PyPackageRequirements
    from pip._internal import main
    main(["install", "-t", script_dir + "/" + vendor_dir, "-r", script_dir + "/" + "requirements.txt", "--quiet"])
    sys.stderr.write('"' + vendor_dir + '" installation completed.\n')

# Add vendorized packages to search path.
site.addsitedir(script_dir + "/" + vendor_dir)
