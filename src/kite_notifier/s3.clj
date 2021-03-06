(ns kite-notifier.s3
  (:import (java.io ByteArrayInputStream))
  (:use [amazonica.aws.s3]))

(defn write-file-to-s3
  ([bucket key data]
   (write-file-to-s3 bucket key data "application/text" false))
  ([bucket key data content-type public?]
   (let [bytes (.getBytes data)
         input-stream (ByteArrayInputStream. bytes)]
     (put-object :bucket-name bucket
                 :key key
                 :input-stream input-stream
                 :return-values "ALL_NEW"
                 :metadata {:content-length (count bytes)
                            :content-type content-type}
                 :access-control-list (when public? {:grant-permission ["AllUsers" "Read"]})))))

(defn write-setting-to-s3 [bucket key]
  (write-file-to-s3 bucket key ""))

(defn read-setting-from-s3 [bucket key]
  (:last-modified (:object-metadata (get-object bucket key))))