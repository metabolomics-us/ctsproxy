/**
 * Created by matthewmueller on 3/16/17.
 */

var app = angular.module('cts', ['ngRoute', 'ngBreakpoint', 'angularFileUpload', 'btorfs.multiselect', 'ui.bootstrap']);

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

