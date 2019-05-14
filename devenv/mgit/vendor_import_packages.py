# ---------------------------------------------------------------------------
#   The confidential and proprietary information contained in this file may
#   only be used by a person authorised under and to the extent permitted
#   by a subsisting licensing agreement from ARM Limited or its affiliates.
#
#          (C) COPYRIGHT 2013-2017 ARM Limited or its affiliates.
#              ALL RIGHTS RESERVED
#
#   This entire notice must be reproduced on all copies of this file
#   and copies of this file may only be made by a person if such person is
#   permitted to do so under the terms of a subsisting license agreement
#   from ARM Limited or its affiliates.
# ---------------------------------------------------------------------------

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
    import pip
    pip.main(["install", "-t", script_dir + "/" + vendor_dir, "-r", script_dir + "/" + "requirements.txt", "--quiet"])
    sys.stderr.write('"' + vendor_dir + '" installation completed.\n')

# Add vendorized packages to search path.
site.addsitedir(script_dir + "/" + vendor_dir)
