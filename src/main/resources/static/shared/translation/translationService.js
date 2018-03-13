(function() {
    'use strict';

    angular
        .module('cts')
        .factory('translation', translation);

    translation.$inject = ['$http'];

    /* @ngInject */
    function translation($http) {

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
                }, function(error) {
                    return error.data;
                });
        }

        function getFromValues() {
            return $http.get('rest/fromValues')
                .then(function(response){
                    return response.data;
                }, function(error) {
                    return error.data;
                });
        }

        function convert(from, to, string) {
            return (to === 'InChIKey') ?
                $http.get('/rest/score/' + encodeURIComponent(from) + '/' + string + '/biological')
                    .then(function(response){
                        if (response.data && response.data.result && response.data.result[0].value === 'no scoring done') {
                            return { result: [] };
                        }
                        return response.data;
                    }, function(error){
                        return error.data;
                    }):
                $http.get('/rest/convert/' + encodeURIComponent(from) + '/' + encodeURIComponent(to) + '/' + string)
                    .then(function(response){
                        return response.data[0];
                    }, function(error){
                        return error.data;
                    });
        }

    }
})();