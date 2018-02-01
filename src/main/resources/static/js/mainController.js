(function() {
    'use strict';

    angular
        .module('cts')
        .controller('MainController', MainController);


    MainController.$inject = ['$scope', '$timeout', '$http', '$q', 'translation', 'FileUploader'];

    function MainController($scope, $timeout, $http, $q, translation, FileUploader) {
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
        vm.exportType = 'csv';

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
            translation.getFromValues()
                .then(function(data){
                    vm.fromValues = data;
                });

            translation.getToValues()
                .then(function(data){
                    vm.toValues = data;
                });
        }

        $scope.$watch(function() { return vm.query; }, function(query) {
            if (query.string !== '') {
                vm.loading = true;

                translation.convert(query.from, query.to, query.string)
                    .then(function(data) {
                        vm.loading = false;
                        vm.results = data.result;
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
                                return translation.convert(query.from, to, string)
                                    .then(function(data) {
                                        vm.batchResults[string][to] = data.result;
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

        /**
         * Create a file for export with the format:
         * From | To | Term | Result | Score
         *
         * @param filetype
         */
        $scope.exportSimplified = function(filetype) {
            var data = 'From,To,Term,Result,Score';

            data += '\n' + vm.results.map(function(result) {
                if (vm.query.to === 'InChIKey') {
                    return vm.query.from + ',' + vm.query.to + ',' + vm.query.string + ',' + result.InChIKey + ',' + result.score;
                } else {
                    return vm.query.from + ',' + vm.query.to + ',' + vm.query.string + ',"' + result + '"';
                }
            }).join('\n');

            function pad2(n) {
                return (n < 10 ? '0' : '') + n;
            }

            var date = new Date();
            date = date.getFullYear() + pad2(date.getMonth() + 1) + pad2(date.getDate()) + pad2(date.getHours()) + pad2(date.getMinutes()) + pad2(date.getSeconds());

            $timeout(function() { downloadData(data, date + '.' + filetype, 'text/' + filetype); }, 100);
        };

        /**
         * Create a file for export with the format:
         * From | To | Term | Result | Score
         *
         * @param filetype
         */
        $scope.exportBatchSimplified = function(filetype) {
            var data = 'From,To,Term,Result,Score';

            for (var searchTerm in vm.batchResults) {
                for (var target in vm.batchResults[searchTerm]) {
                    var resultList = vm.batchResults[searchTerm][target];

                    data += '\n' + resultList.map(function(result) {
                        /**
                         * InChIKey is an object with properties InChIKey and score
                         * Everything else is a string
                         */

                        return target === 'InChIKey' ?
                            vm.query.from + ',' + target + ',' + searchTerm + ',' + result.InChIKey + ',' + result.score :
                            vm.query.from + ',' + target + ',' + searchTerm + ',"' + result + '"';
                    }).join('\n');
                }
            }

            function pad2(n) {
                return (n < 10 ? '0' : '') + n;
            }

            var date = new Date();
            date = date.getFullYear() + pad2(date.getMonth() + 1) + pad2(date.getDate()) + pad2(date.getHours()) + pad2(date.getMinutes()) + pad2(date.getSeconds());

            $timeout(function() { downloadData(data, date + '.' + filetype, 'text/' + filetype); }, 100);
        };

        /**
         * Create a file for export with the format:
         * Search Term | Target Term | Score
         *
         * @param filetype
         */
        $scope.export = function(filetype) {
            var data = vm.query.from + ',' + vm.query.to + ',Score';

            data += '\n' + vm.results.map(function(result) {
                if (vm.query.to === 'InChIKey') {
                    return vm.query.string + ',' + result.InChIKey + ',' + result.score;
                } else {
                    return vm.query.string + ',"' + result + '"';
                }
            }).join('\n');

            function pad2(n) {
                return (n < 10 ? '0' : '') + n;
            }

            var date = new Date();
            date = date.getFullYear() + pad2(date.getMonth() + 1) + pad2(date.getDate()) + pad2(date.getHours()) + pad2(date.getMinutes()) + pad2(date.getSeconds());

            $timeout(function() { downloadData(data, date + '.' + filetype, 'text/' + filetype); }, 100);
        };

        /**
         *
         */
        $scope.exportBatch = function(filetype) {
            var header = vm.query.from;

            vm.batchQuery.to.forEach(function(to) {
                header += ',' + to;

                if (to === 'InChIKey') {
                    header += ',Score';
                }
            });

            var data = header +'\n'+
                Object.keys(vm.batchResults).map(function(searchTerm) {
                    var includeRow = true;
                    var rows = [];

                    for (var rowIndex = 0; includeRow === true; rowIndex++) {
                        includeRow = false;

                        var row = searchTerm + ',' + vm.batchQuery.to.map(function(target) {
                            var result = vm.batchResults[searchTerm][target][rowIndex];
                            if (result) {
                                includeRow = true;

                                return target === 'InChIKey' ?
                                    result.InChIKey + ',' + result.score :
                                    '"' + result + '"';
                            } else {
                                return target === 'InChIKey' ?
                                    ',' :
                                    '';
                            }
                        }).join(',');

                        if (includeRow) {
                            rows.push(row);
                        }
                    }

                    return rows.join('\n');
                }).join('\n');

            // Format date
            function pad2(n) {
                return (n < 10 ? '0' : '') + n;
            }

            var date = new Date();
            date = date.getFullYear() + pad2(date.getMonth() + 1) + pad2(date.getDate()) + pad2(date.getHours()) + pad2(date.getMinutes()) + pad2(date.getSeconds());

            $timeout(function() { downloadData(data, date + '.' + filetype, 'text/' + filetype); }, 100);
        };

        /**
         *
         *
         */
        $scope.exportBatchOneToOne = function(filetype) {
            var header = vm.query.from;

            vm.batchQuery.to.forEach(function(to) {
                header += (to === 'InChIKey') ?
                    ',' + to + ',' + 'Score' :
                    ',' + to;
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

                            return '"' + inchis.join('\n') + '","' + scores.join('\n') + '"';
                        }

                        return '"' + results.join('\n') + '"';
                    }).join(',');
                }).join('\n');

            // Format date
            function pad2(n) {
                return (n < 10 ? '0' : '') + n;
            }

            var date = new Date();
            date = date.getFullYear() + pad2(date.getMonth() + 1) + pad2(date.getDate()) + pad2(date.getHours()) + pad2(date.getMinutes()) + pad2(date.getSeconds());

            $timeout(function() { downloadData(data, date + '.' + filetype, 'text/' + filetype); }, 100);
        }

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