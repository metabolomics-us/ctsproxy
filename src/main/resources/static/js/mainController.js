(function() {
    'use strict';

    angular
        .module('cts')
        .controller('MainController', MainController);


    MainController.$inject = ['$scope', '$timeout', '$http', '$q', '$location', 'translation', 'download', 'FileUploader'];

    function MainController($scope, $timeout, $http, $q, $location, translation, download, FileUploader) {
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
        vm.exportStyle = 'simplified'
        vm.exportType = 'csv';
        vm.topHit = false;

        vm.fromValues = [];
        vm.toValues = [];

        vm.resultCount = 0;
        vm.resultPage = 1;
        vm.pageCount = 1;

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
                        vm.results = {};
                        vm.results[query.string] = {};
                        vm.results[query.string][query.to] = data.result;
                    });
            }
        }, true);

        $scope.$watch(function() { return vm.batchQuery; }, function(query, oldQuery) {
            if (query.string !== '' && query.to.length !== 0 &&
                (query.string !== oldQuery.string || query.to.length !== oldQuery.to.length || query.from !== oldQuery.from)) {

                vm.generation += 1;
                vm.loading = true;
                vm.loadingCounter = 0;
                vm.loadingTotal = 0;
                vm.batchResults = {};

                var myGeneration = vm.generation;
                var queryStrings = query.string.split('\n').filter(Boolean);
                var promise = $q.all(null);

                angular.forEach(queryStrings, function(string) {
                    vm.batchResults[string] = {};
                    angular.forEach(query.to, function(to) {
                        vm.batchResults[string][to] = {};
                        vm.loadingTotal += 1;
                        promise = promise.then(function() {
                            if (vm.generation !== myGeneration) {
                                return $q.reject('Request reset');
                            } else {
                                return translation.convert(query.from, to, string)
                                    .then(function(data) {
                                        vm.batchResults[string][to] = data.result;
                                        if (vm.generation === myGeneration) {
                                            vm.loadingCounter += 1;
                                        }
                                    }).catch(function(error) {
                                        vm.batchResults[string][to] = [];
                                        console.error(error);

                                        return;
                                    });
                            }
                        });
                    });
                });

                promise.then(function() {
                    vm.loading = false;
                    vm.resultCount = Object.keys(vm.batchResults).length;
                    vm.pageCount = Math.ceil(vm.resultCount / 10);
                }).catch(function(error) {
                    console.error(error);
                });

            }
        }, true);

        $scope.batchDownloadService = function() {
            $timeout(function() {
                download.export(vm.batchQuery, vm.batchResults, vm.exportStyle, vm.topHit, vm.exportType);
            }, 100);
        }

        $scope.singleDownloadService = function() {
            $timeout(function() {
                download.export(vm.query, vm.results, vm.exportStyle, vm.topHit, vm.exportType);
            }, 100);
        }
    }

})();