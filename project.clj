(defproject com.googlecode/jmxtrans "1.0.0-SNAPSHOT"
  :min-lein-version "2.0.0"
  :description "JMXTrans (with some additions)"
  :java-source-paths ["src"]
  :local-repo "repo"
  :dependencies [[org.clojure/clojure                     "1.4.0"]
                 [org.clojure/math.combinatorics          "0.0.3"]
                 [commons-cli                             "1.2"]
                 [commons-codec                           "1.3"]
                 [commons-io                              "1.4"]
                 [commons-lang                            "2.5"]
                 [commons-logging                         "1.1.1"]
                 [commons-pool                            "1.5.6"]
                 [log4j                                   "1.2.16"]
                 [net.sourceforge/jpathwatch              "0-94"]
                 [org.apache.velocity/velocity            "1.7"]
                 [org.codehaus.jackson/jackson-core-asl   "1.6.3"]
                 [org.codehaus.jackson/jackson-mapper-asl "1.6.3"]
                 [org.jrobin/jrobin                       "1.5.9"]
                 [org.quartz-scheduler/quartz             "1.8.4"]
                 [org.slf4j/slf4j-api                     "1.6.1"]
                 [org.slf4j/slf4j-log4j12                 "1.6.1"]]
  :aot [com.googlecode.jmxtrans.model.output.graphite-ratio-writer]
  :main com.googlecode.jmxtrans.JmxTransformer)
