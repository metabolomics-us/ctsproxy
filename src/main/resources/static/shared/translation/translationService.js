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
            return $http.get('/rest/toValues')
                .then(function(response){
                    return response.data;
                }, function(err) {
                    return $q.reject(err);
                });
        }

        function getFromValues() {
            return $http.get('/rest/fromValues')
                .then(function(response){
                    return response.data;
                }, function(err) {
                    return $q.reject(err);
                });
        }

        function convert(from, to, string) {
            return (to === 'InChIKey') ?
                $http.get('/rest/score/' + encodeURIComponent(from) + '/' + string + '/biological')
                    .then(function(response) {

                        var result = [];

                        if (response.data && response.data.result) {
                            if (response.data.result.length === 0 || response.data.result[0].value === 'no scoring done') {
                                result.push({ value: 'No result', score: 'N/A' });
                            } else {
                                response.data.result.forEach(function(res) {
                                    result.push({ value: res.InChIKey, score: res.score });
                                });
                            }
                        } else {
                            result.push({ value: 'No result', score: 'N/A' });
                        }

                        return result;

                    }, function(err) {
                        return $q.reject(err);
                    }):
                $http.get('/rest/convert/' + encodeURIComponent(from) + '/' + encodeURIComponent(to) + '/' + string)
                    .then(function(response) {

                        var result = [];

                        if (response.data[0] && response.data[0].result.length > 0) {
                            response.data[0].result.forEach(function(res) {
                                result.push({ value: res });
                            });
                        } else {
                            result.push({ value: 'No result' })
                        }

                        return result;

                    }, function(err) {
                        return $q.reject(err);
                    });
        }

    }
})();