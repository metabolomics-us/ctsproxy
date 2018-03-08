angular.module('cts')
    .config(function($routeProvider, $locationProvider) {
        $routeProvider
            .when("/", {
                templateUrl: "components/single/singleView.html"
            })
            .when("/batch", {
                templateUrl: "components/batch/batchView.html"
            })
            .when("/services", {
                templateUrl: "components/services/servicesView.html"
            });

        $locationProvider.html5Mode(true);
        $locationProvider.hashPrefix("");
    });