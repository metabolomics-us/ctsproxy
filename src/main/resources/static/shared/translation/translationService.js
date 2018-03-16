(function() {
    'use strict';

    angular
        .module('cts')
        .factory('translation', translation);

    translation.$inject = ['$http', '$q'];

    /* @ngInject */
    function translation($http, $q) {

        var service = {
            getToValues: getToValues,
            getFromValues: getFromValues,
            convert: convert
        };

        return service;

        //////////

        function getToValues() {
            return $http.get('rest/toValues')
                .then(function(response){
                    return response.data;
                }, function(err) {
                    return $q.reject(err);
                });
        }

        function getFromValues() {
            return $http.get('rest/fromValues')
                .then(function(response){
                    return response.data;
                }, function(err) {
                    return $q.reject(err);
                });
        }

        function convert(from, to, string) {
            return (to === 'InChIKey') ?
                $http.get('/rest/score/' + encodeURIComponent(from) + '/' + encodeURIComponent(string) + '/biological')
                    .then(function(response) {
                        if (response.data && response.data.result && response.data.result[0].value === 'no scoring done') {
                            return { result: [] };
                        }
                        return response.data;
                    }, function(err) {
                        return $q.reject(err);
                    }):
                $http.get('/rest/convert/' + encodeURIComponent(from) + '/' + encodeURIComponent(to) + '/' + encodeURIComponent(string))
                    .then(function(response) {
                        return response.data[0];
                    }, function(err) {
                        return $q.reject(err);
                    });
        }

    }
})();