(function() {
    'use strict';

    angular.module('cts')
        .controller('MainController', MainController);


    MainController.$inject = ['$scope', '$timeout', '$http', '$q', '$location', '$anchorScroll', 'translation', 'download', 'FileUploader'];

    function MainController($scope, $timeout, $http, $q, $location, $anchorScroll, translation, download, FileUploader) {
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
        };

        vm.generation = 0;
        vm.exportStyle = 'table'
        vm.exportType = 'csv';
        vm.topHit = true;

        vm.fromValues = [];
        vm.toValues = [];
        vm.queryStrings = [];
        vm.errors = [];

        vm.uploader = new FileUploader();

        vm.uploader.onAfterAddingFile = function(file) {
            var reader = new FileReader();

            reader.onload = function(e) {
                $scope.$apply(function() {
                    vm.batchQuery.string = reader.result;
                });
            };

            reader.readAsText(file._file);
        };

        activate();

        //////////

        function activate() {
            translation.getFromValues()
                .then(function(data) {
                    vm.fromValues = data;
                }, function(err) {
                    vm.errors.push(err);
                    console.error(err);
                });

            translation.getToValues()
                .then(function(data) {
                    vm.toValues = data;
                }, function(err) {
                    vm.errors.push(err);
                    console.error(err);
                });
        }

        $scope.convertSingle = function(query) {
            if (query.string !== '') {
                vm.loading = true;
                vm.errors = [];

                translation.convert(query.from, query.to, query.string)
                    .then(function(result) {
                        vm.loading = false;
                        vm.results = {};
                        vm.results[query.string] = {};
                        vm.results[query.string][query.to] = result;
                    }).catch(function(err) {
                        vm.loading = false;
                        vm.errors.push(err);
                    });
            }
        };

        $scope.convertBatch = function(query) {
            if (query.string !== '' && query.to.length !== 0) {

                vm.generation += 1;
                vm.loading = true;
                vm.errors = [];
                vm.loadingCounter = 0;
                vm.loadingTotal = 0;
                vm.batchResults = {};

                var myGeneration = vm.generation;
                vm.queryStrings = query.string.split('\n').filter(Boolean);
                var promise = $q.all(null);

                angular.forEach(vm.queryStrings, function(string) {
                    vm.batchResults[string] = {};
                    angular.forEach(query.to, function(to) {
                        vm.batchResults[string][to] = {};
                        vm.loadingTotal += 1;
                        promise = promise.then(function() {
                            if (vm.generation !== myGeneration) {
                                return $q.reject('Request reset');
                            } else {
                                return translation.convert(query.from, to, string)
                                    .then(function(result) {
                                        vm.batchResults[string][to] = result;
                                        if (vm.generation === myGeneration) {
                                            vm.loadingCounter += 1;
                                        }
                                    }).catch(function(err) {
                                        vm.batchResults[string][to] = [];
                                        vm.errors.push(err);

                                        return;
                                    });
                            }
                        });
                    });
                });

                promise.then(function() {
                    vm.loading = false;
                }).catch(function(err) {
                    console.log(err);
                });

            }
        };

        $scope.batchDownloadService = function() {
            $timeout(function() {
                download.export(vm.batchQuery, vm.queryStrings, vm.batchResults, vm.exportStyle, vm.topHit, vm.exportType);
            }, 100);
        }

        $scope.singleDownloadService = function() {
            $timeout(function() {
                download.export(vm.query, [vm.query.string], vm.results, vm.exportStyle, vm.topHit, vm.exportType);
            }, 100);
        }

        $scope.scrollTo = function(id) {
            $location.hash(id);

            $anchorScroll();
        }

        $scope.toggleShortcutBox = function() {
            if (document.getElementById('shortcut-box-content').style.right == '0px') {
                document.getElementById('shortcut-box-content').style.right = '-300px';
                document.getElementById('shortcut-box-tab').style.right = '-1px';
            } else {
                document.getElementById('shortcut-box-content').style.right = '0px';
                document.getElementById('shortcut-box-tab').style.right = '299px';
            }
        }
    }

})();
