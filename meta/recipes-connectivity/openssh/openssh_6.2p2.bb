SUMMARY = "Secure rlogin/rsh/rcp/telnet replacement"
DESCRIPTION = "Secure rlogin/rsh/rcp/telnet replacement (OpenSSH) \
Ssh (Secure Shell) is a program for logging into a remote machine \
and for executing commands on a remote machine."
HOMEPAGE = "http://openssh.org"
SECTION = "console/network"
LICENSE = "BSD"
LIC_FILES_CHKSUM = "file://LICENCE;md5=e326045657e842541d3f35aada442507"

PR = "r3"

DEPENDS = "zlib openssl"
DEPENDS += "${@base_contains('DISTRO_FEATURES', 'pam', 'libpam', '', d)}"

RPROVIDES_${PN}-ssh = "ssh"
RPROVIDES_${PN}-sshd = "sshd"

RCONFLICTS_${PN} = "dropbear"
RCONFLICTS_${PN}-sshd = "dropbear"
RCONFLICTS_${PN}-keygen = "ssh-keygen"

SRC_URI = "ftp://ftp.openbsd.org/pub/OpenBSD/OpenSSH/portable/openssh-${PV}.tar.gz \
           file://nostrip.patch \
           file://sshd_config \
           file://ssh_config \
           file://openssh-CVE-2011-4327.patch \
           file://mac.patch \
           ${@base_contains('DISTRO_FEATURES', 'pam', '${PAM_SRC_URI}', '', d)} \
           file://sshd.service \
           file://sshdgenkeys.service \
           file://volatiles.99_sshd "

PAM_SRC_URI = "file://sshd"

SRC_URI[md5sum] = "be46174dcbb77ebb4ea88ef140685de1"
SRC_URI[sha256sum] = "7f29b9d2ad672ae0f9e1dcbff871fc5c2e60a194e90c766432e32161b842313b"

inherit useradd update-alternatives systemd

USERADD_PACKAGES = "${PN}-sshd"
USERADD_PARAM_${PN}-sshd = "--system --no-create-home --home-dir /var/run/sshd --shell /bin/false --user-group sshd"

SYSTEMD_PACKAGES = "${PN}-sshd"
SYSTEMD_SERVICE_${PN}-sshd = "sshd.service sshdgenkeys.service"

PACKAGECONFIG ??= "tcp-wrappers"
PACKAGECONFIG[tcp-wrappers] = "--with-tcp-wrappers,,tcp-wrappers"

inherit autotools

# LFS support:
CFLAGS += "-D__FILE_OFFSET_BITS=64"
export LD = "${CC}"

EXTRA_OECONF = "--with-rand-helper=no \
                ${@base_contains('DISTRO_FEATURES', 'pam', '--with-pam', '--without-pam', d)} \
                --without-zlib-version-check \
                --with-privsep-path=/var/run/sshd \
                --sysconfdir=${sysconfdir}/ssh \
                --with-xauth=/usr/bin/xauth"

# This is a workaround for uclibc because including stdio.h
# pulls in pthreads.h and causes conflicts in function prototypes.
# This results in compilation failure, so unless this is fixed,
# disable pam for uclibc.
EXTRA_OECONF_append_libc-uclibc=" --without-pam"

do_configure_prepend () {
	if [ ! -e acinclude.m4 -a -e aclocal.m4 ]; then
		cp aclocal.m4 acinclude.m4
	fi
}
do_compile_append () {
	install -m 0644 ${WORKDIR}/sshd_config ${S}/
	install -m 0644 ${WORKDIR}/ssh_config ${S}/
}

do_install_append () {
	for i in ${DISTRO_FEATURES};
	do
		if [ ${i} = "pam" ];  then
			install -d ${D}${sysconfdir}/pam.d
			install -m 0755 ${WORKDIR}/sshd ${D}${sysconfdir}/pam.d/sshd
		fi
	done
	install -d ${D}/${sysconfdir}/default/volatiles
	install -m 644 ${WORKDIR}/volatiles.99_sshd ${D}/${sysconfdir}/default/volatiles/99_sshd

        # Create config files for read-only rootfs
	install -d ${D}${sysconfdir}/ssh
	install -m 644 ${WORKDIR}/sshd_config ${D}${sysconfdir}/ssh/sshd_config_readonly
	sed -i '/HostKey/d' ${D}${sysconfdir}/ssh/sshd_config_readonly
	echo "HostKey /var/run/ssh/ssh_host_rsa_key" >> ${D}${sysconfdir}/ssh/sshd_config_readonly
	echo "HostKey /var/run/ssh/ssh_host_dsa_key" >> ${D}${sysconfdir}/ssh/sshd_config_readonly
	echo "HostKey /var/run/ssh/ssh_host_ecdsa_key" >> ${D}${sysconfdir}/ssh/sshd_config_readonly

	install -d ${D} ${D}${systemd_unitdir}/system
	install -c -m 0644 ${WORKDIR}/sshd.service ${D}${systemd_unitdir}/system
	install -c -m 0644 ${WORKDIR}/sshdgenkeys.service ${D}${systemd_unitdir}/system
	sed -i -e 's,@BASE_BINDIR@,${base_bindir},g' \
		-e 's,@SBINDIR@,${sbindir},g' \
		-e 's,@BINDIR@,${bindir},g' \
		${D}${systemd_unitdir}/system/*.service

	install -d ${D}/var/run/sshd
}

ALLOW_EMPTY_${PN} = "1"

PACKAGES =+ "${PN}-keygen ${PN}-scp ${PN}-ssh ${PN}-sshd ${PN}-sftp ${PN}-misc ${PN}-sftp-server"
FILES_${PN}-scp = "${bindir}/scp.${BPN}"
FILES_${PN}-ssh = "${bindir}/ssh.${BPN} ${sysconfdir}/ssh/ssh_config"
FILES_${PN}-sshd = "${sbindir}/sshd ${systemd_unitdir}/system/sshd.service ${systemd_unitdir}/system/sshdgenkeys.service"
FILES_${PN}-sshd += "${sysconfdir}/ssh/moduli ${sysconfdir}/ssh/sshd_config ${sysconfdir}/ssh/sshd_config_readonly ${sysconfdir}/default/volatiles/99_sshd"
FILES_${PN}-sftp = "${bindir}/sftp"
FILES_${PN}-sftp-server = "${libexecdir}/sftp-server"
FILES_${PN}-misc = "${bindir}/ssh* ${libexecdir}/ssh*"
FILES_${PN}-keygen = "${bindir}/ssh-keygen"

RDEPENDS_${PN} += "${PN}-scp ${PN}-ssh ${PN}-sshd ${PN}-keygen"
RDEPENDS_${PN}-sshd += "${PN}-keygen"

CONFFILES_${PN}-sshd = "${sysconfdir}/ssh/sshd_config"
CONFFILES_${PN}-ssh = "${sysconfdir}/ssh/ssh_config"

ALTERNATIVE_PRIORITY = "90"
ALTERNATIVE_${PN}-scp = "scp"
ALTERNATIVE_${PN}-ssh = "ssh"

