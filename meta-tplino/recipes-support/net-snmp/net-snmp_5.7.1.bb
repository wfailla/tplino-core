require net-snmp.inc

inherit systemd

PR = "${INC_PR}.7"
LIC_FILES_CHKSUM = "file://README;beginline=3;endline=8;md5=7f7f00ba639ac8e8deb5a622ea24634e"

SRC_URI = "${SOURCEFORGE_MIRROR}/net-snmp/net-snmp-${PV}.tar.gz \
        file://sync-with-5.7-branch.patch \
        file://libnl-3-support.patch \
        file://systemd-support.patch \
        file://snmpd.service \
        file://snmptrapd.service \
        file://snmpd.conf \
        file://snmptrapd.conf"

EXTRA_OECONF += "--disable-embedded-perl --with-perl-modules=no --with-systemd "
EXTRA_OEMAKE = "INSTALL_PREFIX=${D}"

SYSTEMD_PACKAGES = "${PN}-server-snmpd \
                    ${PN}-server-snmptrapd"

SYSTEMD_SERVICE_${PN}-server-snmpd = "snmpd.service"
SYSTEMD_SERVICE_${PN}-server-snmptrapd =  "snmptrapd.service"

do_configure_prepend() {
        gnu-configize -f
        # We better change sources and re-autoconf here, but
        # required autoconf is too new for us.
        sed -e '/echo.*\".*\\\\.*\"/s/echo/echo -e/g' \
            -e 's/tail -1/tail -n 1/g'                \
            -i configure
}

PARALLEL_MAKE = ""
CCACHE = ""

SRC_URI[md5sum] = "c95d08fd5d93df0c11a2e1bdf0e01e0b"
SRC_URI[sha256sum] = "7c71c9650c65b715356547e20ca2dbe6313944278af8cc19c32a5337f46b181f"

RDEPENDS_${PN}-server-snmpd = "${PN}-server-snmpd"
RDEPENDS_${PN}-server-snmptrapd = "${PN}-server-snmptrapd"
