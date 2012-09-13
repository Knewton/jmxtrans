;; Copyright (c) 2011 Knewton and Tim Dysinger

;; Permission is hereby granted, free of charge, to any person
;; obtaining a copy of this software and associated documentation
;; files (the "Software"), to deal in the Software without
;; restriction, including without limitation the rights to use, copy,
;; modify, merge, publish, distribute, sublicense, and/or sell copies
;; of the Software, and to permit persons to whom the Software is
;; furnished to do so, subject to the following conditions:

;; The above copyright notice and this permission notice shall be
;; included in all copies or substantial portions of the Software.

;; THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
;; EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
;; MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
;; NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS
;; BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN
;; ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
;; CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
;; SOFTWARE.

(ns com.googlecode.jmxtrans.model.output.graphite-ratio-writer
  (:require [clojure.math.combinatorics :as mc])
  (:import com.googlecode.jmxtrans.JmxTransformer
           com.googlecode.jmxtrans.model.output.GraphiteWriter
           com.googlecode.jmxtrans.util.JmxUtils
           java.io.PrintWriter)
  (:gen-class
   :extends com.googlecode.jmxtrans.model.output.GraphiteWriter
   :name com.googlecode.jmxtrans.model.output.GraphiteRatioWriter))

(defn field [obj field]
  "Permiscuous access to our super class' private fields"
  (-> (class (GraphiteWriter.))
      (.getDeclaredField (name field))
      (doto (.setAccessible true))
      (.get obj)))

(defn get-key [query type-names root-prefix result pair]
  "Build a graphite-compatible key given the data passed in."
  (let [server-alias (or (-> query .getServer .getAlias)
                         (JmxUtils/cleanupStr
                          (str (-> query .getServer .getHost) "_"
                               (-> query .getServer .getPort))))
        classname-alias (or (.getClassNameAlias result)
                            (.getClassName result))
        type-name-values (->> result .getTypeName
                              (JmxUtils/getConcatedTypeNameValues type-names))
        key-string (->> pair
                        (map key)
                        (interpose "-to-")
                        (cons (str (.getAttributeName result) "."))
                        (apply str))]
    (->> [root-prefix
          server-alias
          classname-alias
          type-name-values
          key-string]
         (filter #(not (nil? %)))
         (interpose ".")
         (apply str))))

(defn graphite-data [this types query results]
  "Return graphite data for all possible permutations of query result
data"
  (let [root (field this :rootPrefix)]
    (for [result results
          pair (mapcat mc/permutations
                       (mc/combinations (.getValues result) 2))]
      (try
        (let [gkey (get-key query types root result pair)
              ratio (->> pair
                         (map #(-> % val str Double.))
                         (apply /))
              time (-> result .getEpoch (/ 1000) long)]
          (str gkey " " (format "%1$.2f" ratio) " " time))
        (catch ArithmeticException _)
        (catch NumberFormatException _)))))

(defn -doWrite [this query]
  "Process the query and send the data to Graphite. Replaces
super-class' doWrite method."
  (let [pool (field this :pool)
        addr (field this :address)
        ;; log (field this :log)
        types (.getTypeNames this)
        results (.getResults query)]
    (let [sock (.borrowObject pool addr)]
      (try
        (doseq [line (graphite-data this types query results)]
          (doto (PrintWriter. (.getOutputStream sock) true)
            (.println line)
            (.flush))
          #_(.debug log line))
        (finally (.returnObject pool addr sock))))))
