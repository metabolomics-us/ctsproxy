(function() {
    'use strict';

    angular
        .module('cts')
        .factory('translation', translation);

    translation.$inject = ['$http', '$q'];

    /* @ngInject */
    function translation($http, $q) {

        //  'to' values that should only be converted to from InChIKey
        var inchikeyOnlyConversions = ['PubChem CID', 'Pubchem SID'];

        // 'to' values from InChIKey that use compound lookup instead of translation, along with property names
        var additionalInChIKeyToValues = {
            'Exact Mass': 'exactmass',
            'Molecular Formula': 'formula',
            'Molecular Weight': 'molweight'
        };

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
            return Object.keys(additionalInChIKeyToValues);
        }

        function getInChIKeyOnlyToValues() {
            return getAdditionalInChIKeyToValues().concat(inchikeyOnlyConversions);
        }

        function convert(from, to, string) {
            if (to === 'InChIKey') {
                // apply scoring to find most relevant InChIKey
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
                    }
                );
            } else if (from === 'InChIKey' && getAdditionalInChIKeyToValues().indexOf(to) > -1) {
                // use compound endpoint if querying InChIKey properties
                return $http.get('/service/compound/' +  string)
                    .then(function (response) {
                        var result = {value: 'No result'};

                        // retrieve compound property for the given to value
                        if (response.data && response.data.hasOwnProperty(additionalInChIKeyToValues[to])) {
                            result.value = response.data[additionalInChIKeyToValues[to]];
                        }

                        return [result];
                    },
                    function (err) {
                        return $q.reject(err);
                    }
                );
            } else {
                // otherwise, use general conversion endpoint
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
                    }
                );
            }
        }
    }
})();
