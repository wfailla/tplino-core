#
# Copyright (C) 2012 Travelping GmbH
#

DESCRIPTION = "TPLINO Software Development Kit"
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://${COREBASE}/LICENSE;md5=3f40d7994397109285ec7b81fdeb3b58 \
                    file://${COREBASE}/meta/COPYING.MIT;md5=3da9cfbcb788c80a0384361b4de20420"
PR = "r1"

ALLOW_EMPTY_${PN} = "1"

require task-sdk-tplino.inc

PACKAGES = "${PN} ${PN}-dbg"

RDEPENDS_${PN} = "\
    task-core-standalone-sdk-target \
    ${SDK-TPLINO} \
    ${SDK-EXTRAS}"
