package healthstack.app.pref

/**
 * An enumeration representing the different stages of the app.
 * Each value has a corresponding title.
 * @param title The title of the stage.
 */
enum class AppStage(val title: String) {
    Onboarding("Integração"),
    SignUp("Inscrever-se"),
    Home("Home"),
    Profile("Perfil"),
    StudyInformation("Informação do estudo"),
    Settings("Configurações"),
    Insights("Insights"),
    Education("Educação");
}
