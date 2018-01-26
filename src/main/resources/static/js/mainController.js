(function() {
    'use strict';

    angular
        .module('cts')
        .controller('MainController', MainController);


    MainController.$inject = ['$scope', '$timeout', '$http', '$q', 'FileUploader'];

    function MainController($scope, $timeout, $http, $q, FileUploader) {
        var vm = this;

        vm.query = {
            string: '',
            from: 'Chemical Name',
            to: 'InChIKey'
        };

        vm.batchQuery = {
            string: '',
            from: 'Chemical Name',
            to: ['InChIKey']
        }

        vm.generation = 0;

        vm.fromValues = [];
        vm.toValues = [];

        vm.uploader = new FileUploader();

        vm.uploader.onAfterAddingFile = function(file) {
            var reader = new FileReader();

            reader.onload = function(e) {
                $scope.$apply(function() {
                    vm.batchQuery.string = reader.result;
                });
            };

            reader.readAsText(file._file);
        }

        activate();

        //////////

        function activate() {
            $http.get('rest/fromValues')
                .then(function(response){
                    vm.fromValues = response.data;
                });

            $http.get('rest/toValues')
                .then(function(response){
                    vm.toValues = response.data;
                });
        }

        $scope.$watch(function() { return vm.query; }, function(query) {
            if (query.string !== '') {
                vm.loading = true;

                var url = '/rest/convert/' + encodeURIComponent(query.from) + '/' + encodeURIComponent(query.to) + '/' + query.string;

                if (query.to === 'InChIKey') {
                    url = '/rest/score/' + encodeURIComponent(query.from) + '/' + query.string + '/popularity';
                }

                $http.get(url)
                    .then(function(response) {
                        vm.loading = false;
                        vm.results = response.data[0] ? response.data[0].result : response.data.result;
                    });
            }
        }, true);

        $scope.$watch(function() { return vm.batchQuery; }, function(query, oldQuery) {
            if (query.string !== '' && query.to.length !== 0 && (
                query.string !== oldQuery.string || query.to.length !== oldQuery.to.length || query.from !== oldQuery.from)) {

                vm.generation += 1;
                vm.loading = true;
                vm.loadingCounter = 0;
                vm.loadingTotal = 0;
                vm.batchResults = {};

                var myGeneration = vm.generation;

                var queryStrings = query.string.replace(/\n/g, ',').split(',');

                var promise = $q.all(null);

                angular.forEach(queryStrings, function(string) {
                    vm.batchResults[string] = {};
                    angular.forEach(query.to, function(to) {
                        vm.batchResults[string][to] = {};
                        vm.loadingTotal += 1;
                        promise = promise.then(function() {

                            if (vm.generation !== myGeneration) {
                                return $q.reject('New request made');
                            } else {
                                var url = '/rest/convert/' + encodeURIComponent(query.from) + '/' + encodeURIComponent(to) + '/' + string;

                                if (to === 'InChIKey') {
                                    url = '/rest/score/' + encodeURIComponent(query.from) + '/' + string + '/popularity';
                                }

                                return $http.get(url).then(function(response) {
                                    vm.batchResults[string][to] = response.data[0] ? response.data[0].result : response.data.result;
                                    if (vm.generation === myGeneration) {
                                        vm.loadingCounter += 1;
                                    }
                                });
                            }
                        });
                    });
                });

                promise.then(function() {
                    vm.loading = false;
                });

            }
        }, true);

        $scope.exportSingle = function() {
            var header = vm.query.from + ',' + vm.query.to;
            var data;

            if (vm.query.to === 'InChIKey') {
                header += ',Score';
                data = header +'\n'+
                    vm.results.map(function(x) {
                        return [vm.query.string].concat(x.InChIKey).concat(x.score).join(',');
                    }).join('\n');
            } else {
                data = header +'\n'+
                    vm.results.map(function(x) {
                       return [vm.query.string].concat(x).join(',');
                    }).join('\n');
            }

            // Format date
            function pad2(n) {
                return (n < 10 ? '0' : '') + n;
            }

            var date = new Date();
            date = date.getFullYear() + pad2(date.getMonth() + 1) + pad2(date.getDate()) + pad2(date.getHours()) + pad2(date.getMinutes()) + pad2(date.getSeconds());

            downloadData(data, date +'.csv', 'text/csv');
        };

        $scope.exportBatch = function() {
            var header = vm.query.from;

            vm.batchQuery.to.forEach(function(to) {
                header += ',' + to;

                if (to === 'InChIKey') {
                    header += ',Score';
                }
            });

            var data = header +'\n'+
                Object.keys(vm.batchResults).map(function(searchTerm) {
                    return searchTerm + ',' + vm.batchQuery.to.map(function(target) {
                        var results = vm.batchResults[searchTerm][target];

                        if (results.length && results[0].InChIKey) {
                            var inchis = [],
                                scores = [];

                            results.forEach(function(result) {
                                inchis.push(result.InChIKey);
                                scores.push(result.score);
                            });

                            return '"' + inchis.join(',') + '","' + scores.join(',') + '"';
                        }

                        return '"' + results.join(',') + '"';
                    }).join(',');
                }).join('\n');

            // Format date
            function pad2(n) {
                return (n < 10 ? '0' : '') + n;
            }

            var date = new Date();
            date = date.getFullYear() + pad2(date.getMonth() + 1) + pad2(date.getDate()) + pad2(date.getHours()) + pad2(date.getMinutes()) + pad2(date.getSeconds());

            $timeout(function() { downloadData(data, date +'.csv', 'text/csv'); }, 100);
        };

        /**
         * Emulate the downloading of a file given its contents and name
         * @param data
         * @param filename
         * @param mimetype
         */
        function downloadData(data, filename, mimetype) {
            var hiddenElement = document.createElement('a');

            hiddenElement.href = 'data:'+ mimetype +',' + encodeURI(data);
            hiddenElement.target = '_blank';
            hiddenElement.download = filename;

            document.body.appendChild(hiddenElement);
            hiddenElement.click();
            document.body.removeChild(hiddenElement);
        };
    }

})();