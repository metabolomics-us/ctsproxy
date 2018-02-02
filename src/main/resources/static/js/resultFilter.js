(function(){
    'use strict';
    angular
        .module('cts')
        .filter('limitBatchResults', [function() {
            return function(obj, limit, start) {
                var keys = Object.keys(obj);
                if (keys.length < 1) {
                    return [];
                }

                var ret = new Object,
                    count = 0;

                angular.forEach(keys, function(key, arrayIndex) {
                    if (count >= limit + start) {
                        return false;
                    } else if (count >= start) {
                        ret[key] = obj[key];
                    }
                    count++;
                });
                return ret;
            }
        }]);
})();