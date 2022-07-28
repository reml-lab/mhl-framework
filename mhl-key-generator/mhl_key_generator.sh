#!/usr/bin/env bash

set -Eeuo pipefail
trap cleanup SIGINT SIGTERM ERR EXIT

script_dir=$(cd "$(dirname "${BASH_SOURCE[0]}")" &>/dev/null && pwd -P)

usage() {
  cat <<EOF
Usage: $(basename "${BASH_SOURCE[0]}") [-h] [-v] -b param_value

Generate keys for mhl-ingest and companion android apps

Available options:

-h, --help      Print this help and exit
-v, --verbose   Print script debug info
-b, --bks       Name of BKS keystore (output) file
-p, --provider  Path to BouncyCastle provider
EOF
  exit
}

cleanup() {
  trap - SIGINT SIGTERM ERR EXIT
  # script cleanup here
}

setup_colors() {
  if [[ -t 2 ]] && [[ -z "${NO_COLOR-}" ]] && [[ "${TERM-}" != "dumb" ]]; then
    NOFORMAT='\033[0m' RED='\033[0;31m' GREEN='\033[0;32m' ORANGE='\033[0;33m' BLUE='\033[0;34m' PURPLE='\033[0;35m' CYAN='\033[0;36m' YELLOW='\033[1;33m'
  else
    NOFORMAT='' RED='' GREEN='' ORANGE='' BLUE='' PURPLE='' CYAN='' YELLOW=''
  fi
}

msg() {
  echo >&2 -e "${1-}"
}

#msg_n() {
#  echo >&2 -n "${1-}"
#}

die() {
  local msg=$1
  local code=${2-1} # default exit status 1
  msg "$msg"
  exit "$code"
}

parse_params() {
  # default values of variables set from params
  jks='mhl_ingest_serverkeys'
  bks='mhl_ingest_serverkeys.bks'
  provider='bcprov-jdk15on-169.jar'
  psf='mhl_ingest_keyStorePassword'

  while :; do
    case "${1-}" in
    -h | --help) usage ;;
    -v | --verbose) set -x ;;
    --no-color) NO_COLOR=1 ;;
    -b | --bks) # example named parameter
      bks="${2-}"
      shift
      ;;
    -p | --provider)
      provider="${2-}"
      shift
      ;;
    -?*) die "Unknown option: $1" ;;
    *) break ;;
    esac
    shift
  done

  args=("$@")

  # check required params and arguments
#  [[ -z "${param-}" ]] && die "Missing required parameter: param"
#  [[ ${#args[@]} -eq 0 ]] && die "Missing script arguments"

  return 0
}

parse_params "$@"
setup_colors

msg "\n${GREEN}MHL Keystore Generation${NOFORMAT}"
msg "This program will generate a keystore and password for secure data transmission."
msg "The keystore is used both in MHL Server components and in MHL Android app components."
msg "This program will generate a new keystore and password, deleting previously generated items."
msg "Android apps will need to be re-compiled to use the new credentials.\n"

read -p "Are you sure you want to continue? [y/n] " -n 1 -r
echo    # (optional) move to a new line
if [[ ! $REPLY =~ ^[Yy]$ ]]
then
    exit 1
fi

#remove old keystore if one exists
rm -f $jks $bks $psf

echo ""
read -p "Enter keystore password: " password

echo "$password" >> $psf

# generate keystore here
keytool -genkey -alias mhlserver -keyalg RSA -keystore ${jks} -storepass:file $psf -keypass:file $psf

msg "${GREEN}Keystore saved to ${jks}${NOFORMAT}"

msg "${GREEN}Starting JKS -> BKS conversion${NOFORMAT}"

keytool -importkeystore -srckeystore ${jks} -destkeystore ${bks} -deststoretype BKS -provider org.bouncycastle.jce.provider.BouncyCastleProvider -providerpath ${provider} -storepass:file $psf -keypass:file $psf -srcstorepass:file $psf 

msg "${GREEN}Distributing keys ...${NOFORMAT}"

#Distribute keys for use in server
cp "${jks}" ../mhl-secrets/
cp "${psf}" ../mhl-secrets/

#Distribute keys for use in Android apps
cp "${bks}" ../mhl-android-src/mhl-Library/lib/src/main/res/raw/keystore
cp "${psf}" ../mhl-android-src/mhl-Library/lib/src/main/res/raw/keystore_password

msg "${GREEN}Key generation and distribution complete. You must now recompile Android apps using these keys.${NOFORMAT}"