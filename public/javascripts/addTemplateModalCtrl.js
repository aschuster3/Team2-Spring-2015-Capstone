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
    }

  }]);