(function() {
    'use strict';

    angular
        .module('cts')
        .controller('MainController', MainController);


    MainController.$inject = ['$scope', '$http', '$q', 'FileUploader'];

    function MainController($scope, $http, $q, FileUploader) {
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

                $http.get(url)
                    .then(function(response) {
                        vm.loading = false;
                        vm.results = response.data[0].result;
                        if (response.data[0].result.length === 0) {
                            vm.results = ['No results'];
                        }
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
                                return $http.get('/rest/convert/' + encodeURIComponent(query.from) + '/' + encodeURIComponent(to) + '/' + string).then(function(response) {
                                    if (response.data[0].result.length > 0) {
                                        vm.batchResults[string][to] = response.data[0].result;
                                    } else {
                                        vm.batchResults[string][to] = ['No results'];
                                    }
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


        //$scope.fromOptions = [];
        //$scope.from = 'Chemical Name';
    //    $http.get('rest/fromValues')
    //        .then(function(response) {
    //            console.log(response);
    //            //$scope.fromOptions = response.data;
    //            $scope.sources = response.data;
    //        });

        //$scope.toOptions = [];
        //$scope.batchToOptions = [];
        //$scope.preferredToOptions = ['BioCyc','CAS','ChEBI','Chemical Name','Human Metabolome Database','InChI Code','InChIKey','KEGG','LipidMAPS','PubChem CID','ChemSpider'];
        //$scope.to = 'InChIKey';
        //$scope.targets = [];
    //    $scope.toggleSelection = function(option) {
    //        console.log('toggling',option);
    //        var idx = $scope.toSelection.indexOf(option);
    //
    //        // Is currently selected
    //        if (idx > -1) {
    //            $scope.toSelection.splice(idx, 1);
    //        }
    //
    //        // Is newly selected
    //        else {
    //            $scope.toSelection.push(option);
    //        }
    //    };

    //    $http.get('rest/toValues')
    //        .then(function(response) {
    //            response.data.forEach(function(value) {
    //                $scope.targets.push({name: value, selected: false});
    //            });
    //
    //
    //
    //            //console.log(response);
    //            //$scope.toOptions = response.data;
    //            //$scope.batchToOptions = response.data.filter(function (i) {return $scope.preferredToOptions.indexOf(i) < 0;});
    //        });

    //    $scope.simpleConversion = function(searchTerm,from,to) {
    //        $http.get('/rest/convert/'+from+'/'+to+'/'+searchTerm)
    //            .then(function(response) {
    //                console.log(response);
    //                $scope.response = response;
    //            });
    //    };

    //    $scope.batchConversion = function(searchTerms,from,toSelection) {
    //        console.log(toSelection);
    //        $scope.showMore = false;
    //        var terms = searchTerms.split('\n');
    //        toSelection.forEach(function (to,toIndex) {
    //            terms.forEach(function (term,termIndex) {
    //                $scope.response.data[termIndex][toIndex] = {'searchTerm':term,'result':['Loading...']};
    //                $http.get('/rest/convert/'+from+'/'+to+'/'+term)
    //                    .then(function(response) {
    //                        console.log(response);
    //                        $scope.response.data[termIndex][toIndex] = response.data[0];
    //                        if (response.data[0].result.length == 0) {
    //                            $scope.response.data[termIndex][toIndex].result = ['None found'];
    //                        }
    //                    }, function(response) {
    //                        console.log(response);
    //                        $scope.response.data[termIndex][toIndex].result = ['Error'];
    //                    });
    //            });
    //        });
    //    };

        $scope.export = function() {
            var header = 'Search Term,Results';

            var data = header +'\n'+
                $scope.response.data.map(function(x) {
                    return [x.searchTerm].concat(x.result).join(',');
                }).join('\n');

            // Format date
            function pad2(n) {
                return (n < 10 ? '0' : '') + n;
            }

            var date = new Date();
            date = date.getFullYear() + pad2(date.getMonth() + 1) + pad2(date.getDate()) + pad2(date.getHours()) + pad2(date.getMinutes()) + pad2(date.getSeconds());

            downloadData(data, date +'.csv', 'text/csv');
        };

        /**
         * Emulate the downloading of a file given its contents and name
         * @param data
         * @param filename
         * @param mimetype
         */
        var downloadData = function(data, filename, mimetype) {
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