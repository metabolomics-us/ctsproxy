(function() {
    'use strict';

    angular.module('cts')
        .controller('MainController', MainController);


    MainController.$inject = ['$scope', '$timeout', '$http', '$q', '$location', '$anchorScroll', 'translation', 'download', 'FileUploader', 'Analytics'];

    function MainController($scope, $timeout, $http, $q, $location, $anchorScroll, translation, download, FileUploader, Analytics) {
        var vm = this;

        vm.query = {
            string: '',
            from: 'Chemical Name',
            to: 'InChIKey'
        };

        vm.batchQuery = {
            string: '',
            from: 'Chemical Name',
            to: []
        };

        vm.generation = 0;
        vm.exportStyle = 'table';
        vm.exportType = 'csv';
        vm.topHit = true;

        vm.fromValues = [];
        vm.toValues = [];
        vm.singleToValues = [];
        vm.batchToValues = [];
        vm.queryStrings = [];
        vm.errors = [];

        function filterIllegal(array, from) {
            if (from !== 'InChIKey') {
                return array.filter(function(elem){ return elem !== 'PubChem CID' && elem !== 'Pubchem SID'; });
            } else {
                return array;
            }
        }

        $scope.$watch(function() { return vm.batchQuery.from; }, function(newVal) {
            vm.batchToValues = filterIllegal(vm.toValues, newVal);

            if (newVal !== 'InChIKey') {

                var batchTo = vm.batchQuery.to.slice();

                var cidIndex = batchTo.findIndex(function(elem) { return elem === 'PubChem CID'; });

                if (cidIndex > -1) {
                    batchTo.splice(cidIndex, 1);
                }

                var sidIndex = batchTo.findIndex(function(elem) { return elem === 'Pubchem SID'; });

                if (sidIndex > -1) {
                    batchTo.splice(sidIndex, 1);
                }

                vm.batchQuery.to = batchTo;

            }
        });

        $scope.$watch(function() { return vm.query.from; }, function(newVal) {
            vm.singleToValues = filterIllegal(vm.toValues, newVal);

            if (newVal !== 'InChIKey') {
                if (vm.query.to === 'PubChem CID' || vm.query.to === 'Pubchem SID') {
                    vm.query.to = 'InChIKey';
                }
            }
        });

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
                    vm.fromValues = filterIllegal(data, '');
                }, function(err) {
                    vm.errors.push(err);
                    console.error(err);
                });

            translation.getToValues()
                .then(function(data) {
                    vm.toValues = data;
                    vm.singleToValues = filterIllegal(data, vm.query.from);
                    vm.batchToValues = filterIllegal(data, vm.batchQuery.from);
                    vm.batchQuery.to = ['InChIKey'];
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

                Analytics.trackEvent('convert', vm.query.from, vm.query.to, 1);
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

                Analytics.trackEvent('convert', vm.batchQuery.from, vm.batchQuery.to.join('/'), vm.queryStrings.length);
            }
        };

        $scope.batchDownloadService = function() {
            $timeout(function() {
                download.export(vm.batchQuery, vm.queryStrings, vm.batchResults, vm.exportStyle, vm.topHit, vm.exportType);
                Analytics.trackEvent('download', vm.exportType, vm.queryStrings.length);
            }, 100);
        };

        $scope.singleDownloadService = function() {
            $timeout(function() {
                download.export(vm.query, [vm.query.string], vm.results, vm.exportStyle, vm.topHit, vm.exportType);
                Analytics.trackEvent('download', vm.exportType, 1);
            }, 100);
        };

        $scope.scrollTo = function(id) {
            $location.hash(id);
            $anchorScroll();
        };

        $scope.toggleShortcutBox = function() {
            if (document.getElementById('shortcut-box-content').style.right == '0px') {
                document.getElementById('shortcut-box-content').style.right = '-300px';
                document.getElementById('shortcut-box-tab').style.right = '-1px';
            } else {
                document.getElementById('shortcut-box-content').style.right = '0px';
                document.getElementById('shortcut-box-tab').style.right = '299px';
            }
        };
    }
})();
