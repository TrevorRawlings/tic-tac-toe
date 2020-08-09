set -eou pipefail

java -cp ${JAR_FILE} clojure.main -m "$@"
