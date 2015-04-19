angular.module('mwl.calendar')
  .controller('AddTemplateModalCtrl', ['$scope', '$modalInstance', 'Sessions', 'scheduleTemplatesService', function ($scope, $modalInstance, Sessions, scheduleTemplatesService) {

    $scope.form = { };
    $scope.form.startDate = null;
    $scope.form.selectedScheduleTemplate = null;

    $scope.form.scheduleTemplateOptions = [];

    $scope.form.showDatePicker = false;

    $scope.toggleDatePicker = function ($event) {
      $event.preventDefault();
      $event.stopPropagation();
      $scope.form.showDatePicker = !$scope.form.showDatePicker;
    };

    $scope.isSelectedTemplateStartDay = function (date) {
      if ($scope.form.selectedScheduleTemplate === null) {
        return false;
      }
      return date.getDay() === $scope.form.selectedScheduleTemplate.startDay;
    };

    $scope.clickSubmit = createSessionsFromScheduleTemplate;
    $scope.clickCancel = closeModal;

    init();



    function createSessionsFromScheduleTemplate () {
      Sessions.createFromScheduleTemplate($scope.form.selectedScheduleTemplate, $scope.form.startDate);
      closeModal();  // on success
    }

    function closeModal () {
      $modalInstance.dismiss('cancel');
    }

    function init() {
      scheduleTemplatesService.getAll().then(function success(scheduleTemplates) {
        $scope.form.scheduleTemplateOptions = scheduleTemplates;
      });

      $scope.$watch('form.selectedScheduleTemplate', function () {
        console.log("Triggering a datepicker refresh...");
        $scope.$broadcast('refreshDatepickers');
      })
    }

  }]);