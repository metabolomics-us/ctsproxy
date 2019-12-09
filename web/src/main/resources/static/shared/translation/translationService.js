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
            getInChIKeyOnlyToValues: getInChIKeyOnlyToValues,
            convert: convert
        };

        return service;

        //////////

        function getToValues() {
            return $http.get('/rest/toValues')
                .then(function(response){
                    return response.data.concat(getAdditionalInChIKeyToValues());
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

        function getAdditionalInChIKeyToValues() {
            return ['Exact Mass', 'Molecular Formula', 'Molecular Weight'];
        }

        function getInChIKeyOnlyToValues() {
            return getAdditionalInChIKeyToValues().concat(['PubChem CID', 'Pubchem SID']);
        }

        function convert(from, to, string) {
            if (to === 'InChIKey') {
                return $http.get('/rest/score/' + encodeURIComponent(from) + '/' + string + '/biological')
                    .then(function (response) {
                        var result = [];

                        if (response.data && response.data.result) {
                            if (response.data.result.length === 0 || response.data.result[0].value === 'no scoring done') {
                                result.push({value: 'No result', score: 'N/A'});
                            } else {
                                response.data.result.forEach(function (res) {
                                    result.push({value: res.InChIKey, score: res.score});
                                });
                            }
                        } else {
                            result.push({value: 'No result', score: 'N/A'});
                        }

                        return result;
                    },
                    function (err) {
                        return $q.reject(err);
                    });
            } else if (from === 'InChIKey' && getAdditionalInChIKeyToValues().indexOf(to) > -1) {
                //
                return $http.get('/service/compound/' +  string)
                    .then(function (response) {
                        var result = {value: 'No result'};

                        if (response.data) {
                            if (to === 'Exact Mass' && response.data.hasOwnProperty('exactmass')) {
                                result.value = response.data.exactmass;
                            }

                            if (to === 'Molecular Formula' && response.data.hasOwnProperty('formula')) {
                                result.value = response.data.formula;
                            }

                            if (to === 'Molecular Weight' && response.data.hasOwnProperty('molweight')) {
                                result.value = response.data.molweight;
                            }
                        }

                        return [result];
                    },
                    function (err) {
                        return $q.reject(err);
                    });
            } else {
                return $http.get('/rest/convert/' + encodeURIComponent(from) + '/' + encodeURIComponent(to) + '/' + string)
                    .then(function (response) {
                        var result = [];

                        if (response.data[0] && response.data[0].results.length > 0) {
                            response.data[0].results.forEach(function (res) {
                                result.push({value: res});
                            });
                        } else {
                            result.push({value: 'No result'});
                        }

                        return result;
                    },
                    function (err) {
                        return $q.reject(err);
                    });
            }
        }
    }
})();
