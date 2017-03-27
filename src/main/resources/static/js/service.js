/**
 * Created by matthewmueller on 3/16/17.
 */

var app = angular.module('serviceApp', ['ngRoute']);

app.config(function($routeProvider,$locationProvider) {
    $locationProvider.hashPrefix("");
    $routeProvider
        .when("/", {
            templateUrl: "views/main.htm"
        })
        .when("/batch", {
            templateUrl: "views/batch.htm"
        });
});

app.controller('MainCtrl', ['$scope','$http', function($scope,$http) {

    $scope.response = {data: []};

    $scope.fromOptions = [];
    $scope.from = 'Chemical Name';
    $http.get('rest/fromValues')
        .then(function(response) {
            console.log(response);
            $scope.fromOptions = response.data;
        });

    $scope.toOptions = [];
    $scope.to = 'InChIKey';
    $http.get('rest/toValues')
        .then(function(response) {
            console.log(response);
            $scope.toOptions = response.data;
        });

    $scope.simpleConversion = function(searchTerm,from,to) {
        $http.get('/rest/convert/'+from+'/'+to+'/'+searchTerm)
            .then(function(response) {
                console.log(response);
                $scope.response = response;
            });
    };

    $scope.batchConversion = function(searchTerm,from,to) {
        var terms = searchTerm.split('\n');
        terms.forEach(function (term,i) {
            $scope.response.data[i] = {'searchTerm':term,'result':['Loading...']};
            $http.get('/rest/convert/'+from+'/'+to+'/'+term)
                .then(function(response) {
                    console.log(response);
                    $scope.response.data[i] = response.data[0];
                    if (response.data[0].result.length == 0) {
                        $scope.response.data[i].result = ['None found'];
                    }
                }, function(response) {
                    console.log(response);
                    $scope.response.data[i].result = ['Error'];
                });
        });
    };
}]);

