package healthstack.sample

import android.content.Context
import android.content.res.Configuration
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import healthstack.kit.auth.SignInProvider.Google
import healthstack.kit.task.base.ImageArticleModel
import healthstack.kit.task.onboarding.OnboardingTask
import healthstack.kit.task.onboarding.model.ConsentTextModel
import healthstack.kit.task.onboarding.model.EligibilityIntroModel
import healthstack.kit.task.onboarding.model.EligibilityIntroModel.EligibilityCondition
import healthstack.kit.task.onboarding.model.EligibilityIntroModel.ViewType.Card
import healthstack.kit.task.onboarding.model.EligibilityResultModel
import healthstack.kit.task.onboarding.model.IntroModel
import healthstack.kit.task.onboarding.model.IntroModel.IntroSection
import healthstack.kit.task.onboarding.step.ConsentTextStep
import healthstack.kit.task.onboarding.step.EligibilityCheckerStep
import healthstack.kit.task.onboarding.step.EligibilityIntroStep
import healthstack.kit.task.onboarding.step.EligibilityResultStep
import healthstack.kit.task.onboarding.step.IntroStep
import healthstack.kit.task.onboarding.view.ConsentTextView
import healthstack.kit.task.onboarding.view.EligibilityIntroView
import healthstack.kit.task.onboarding.view.EligibilityResultView
import healthstack.kit.task.onboarding.view.IntroView
import healthstack.kit.task.signup.SignUpTask
import healthstack.kit.task.signup.model.RegistrationCompletedModel
import healthstack.kit.task.signup.model.SignUpModel
import healthstack.kit.task.survey.question.model.ChoiceQuestionModel
import healthstack.kit.task.survey.question.model.ChoiceQuestionModel.ViewType.Dropdown
import healthstack.kit.task.survey.question.model.QuestionModel
import healthstack.kit.theme.AppColors
import healthstack.kit.theme.mainDarkColors
import healthstack.kit.theme.mainLightColors
import healthstack.sample.R.drawable
import healthstack.sample.registration.RegistrationModel
import healthstack.sample.registration.RegistrationStep
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object OnboardingModule {

    private fun isNightMode(context: Context): Boolean =
        context.resources.configuration.uiMode.and(Configuration.UI_MODE_NIGHT_MASK) == Configuration
            .UI_MODE_NIGHT_YES

    @Singleton
    @Provides
    fun providesAppColors(@ApplicationContext context: Context): AppColors =
        if (isNightMode(context)) mainDarkColors() else mainLightColors()

    @Singleton
    @Provides
    fun provideOnboardingTask(
        introStep: IntroStep,
        eligibilityIntroStep: EligibilityIntroStep,
        eligibilityCheckerStep: EligibilityCheckerStep,
        eligibilityResultStep: EligibilityResultStep,
        consentTextStep: ConsentTextStep,
    ): OnboardingTask =
        OnboardingTask(
            "onboarding-task",
            "Sample-App-On-Boarding",
            "Introduce the project and determine whether the user is able to participate in this project or not.",
            introStep,
            eligibilityIntroStep,
            eligibilityCheckerStep,
            eligibilityResultStep,
            consentTextStep,
        )

    @Singleton
    @Provides
    fun provideSignUpTask(): SignUpTask =
        SignUpTask(
            "signup-task",
            "Sign Up",
            "",
            signUp(),
            registrationCompleted(),
            registrationStep()
        )

    private fun registrationStep(): RegistrationStep =
        RegistrationStep(RegistrationModel("Registration", eligibilityQuestions))

    @Singleton
    @Provides
    fun provideIntroStep(@ApplicationContext context: Context): IntroStep =
        IntroStep(
            "intro-step",
            "Intro-Step",
            intro(context),
            IntroView("Iniciar"),
        )

    @Singleton
    @Provides
    fun provideEligibilityIntroStep(@ApplicationContext context: Context): EligibilityIntroStep =
        EligibilityIntroStep(
            "eligibility-intro-step",
            "Eligibility-Intro-Step",
            eligibilityIntro(context),
            EligibilityIntroView(),
        )

    // TODO: add EligibilityCheckerView in builder
    @Singleton
    @Provides
    fun provideEligibilityCheckerStep(@ApplicationContext context: Context): EligibilityCheckerStep =
        EligibilityCheckerStep.Builder("Elegibilidade")
            .addQuestions(eligibilityQuestions).build()

    @Singleton
    @Provides
    fun provideEligibilityResultStep(@ApplicationContext context: Context): EligibilityResultStep =
        EligibilityResultStep(
            "eligibility-result-step",
            "Eligibility-Result-Step",
            eligibilityResult(context),
            EligibilityResultView(),
        )

    @Singleton
    @Provides
    fun provideConsentTextStep(
        @ApplicationContext context: Context,
    ): ConsentTextStep =
        ConsentTextStep(
            "consent-text-step",
            "Consent-Text-Step",
            consentText(context),
            ConsentTextView()
        )

    private fun intro(@ApplicationContext context: Context) = IntroModel(
        id = "intro",
        title = "CardioFlow",
        drawableId = drawable.sample_image_alpha4,
        logoDrawableId = drawable.ic_launcher,
        summaries = listOf(
            drawable.ic_watch to "Use seu\nrelógio",
            drawable.ic_alert to "10 min\npor dia",
            drawable.ic_home_task to "2 pesquisas\npor semana"
        ),
        sections = listOf(
            IntroSection(
                "Visão Geral",

                "CardioFlow é um estudo desenvolvido pela Universidade da Califórnia, São Francisco." +
                    "Por meio deste estudo, identificamos e " +
                    "medimos os dados de seus sinais vitais e relatórios de sintomas.\n\n" +
                    "Com sua ajuda, poderíamos testar nossos algoritmos e " +
                    "desenvolver tecnologia que contribui para a prevenção doenças cardiovasculares nos E.U.A.",

                ),
            IntroSection(
                "Como participar",
                "Use o relógio o máximo possível e faça medições ativas 3 vezes ao dia quando notificado."
            )
        )
    )

    private fun eligibilityIntro(@ApplicationContext context: Context) = EligibilityIntroModel(
        id = "eligibility",
        title = "Elegibilidade",
        description = "Para começar, faremos algumas perguntas " +
            "para garantir que você é elegível para participar deste estudo.",
        conditions = eligibilitySections,
        viewType = Card
    )

    private fun eligibilityResult(@ApplicationContext context: Context) = EligibilityResultModel(
        id = "eligibility",
        title = "Elegibilidade",
        successModel = eligibilitySuccessMessage,
        failModel = eligibilityFailMessage,
    )

    private fun consentText(
        @ApplicationContext context: Context,
    ) = ConsentTextModel(
        id = "consent",
        title = "Consentimento Informado",
        subTitle = "",
        description = "Leia os Termos de Serviço e Política de Privacidade aqui.",
        checkBoxTexts = listOf(
            "Li toda a informação acima e concordo em me juntar ao estudo.",
            "Concordo em compartilhar meus dados com a Samsung.",
            "Concordo em compartilhar meus dados com os assistentes de pesquisa do estudo."
        )
    )

    private fun signUp() = SignUpModel(
        id = "sign-up-model",
        title = "CardioFlow",
        listOf(Google),
        description = "Obrigado por participar do estudo!\n\n" +
            "Agora, por favor, crie uma conta para acompanhar" +
            "seus dados e mantê-los seguros.",
        drawableId = drawable.ic_launcher
    )

    private fun registrationCompleted() =
        RegistrationCompletedModel(
            id = "registration-completed-model",
            title = "Você terminou!",
            buttonText = "Continuar",
            description = "Parabéns! Está tudo pronto para você. " +
                "Agora, por favor, toque no botão abaixo para iniciar sua jornada CardioFlow!",
            drawableId = drawable.sample_image_alpha1
        )

    private val eligibilitySections: List<EligibilityIntroModel.EligibilityCondition> = listOf(
        EligibilityCondition(
            "Elegibilidade médica",
            listOf("Condição(s) pré-existente(s)", "Prescrição(s)", "Morando nos Estados Unidos")
        ),
        EligibilityCondition(
            "Perfil Básico",
            listOf("Idade", "Localização geográfica", "Dispositivos")
        ),
    )

    private val eligibilitySuccessMessage: ImageArticleModel = ImageArticleModel(
        id = "eligibility",
        title = "Ótimo, você está dentro!",
        description = "Parabéns! Você é elegível para o estudo. " +
            "Em seguida, precisaremos coletar o seu consentimento, e você estará pronto para começar.",
        drawableId = drawable.sample_image_alpha1
    )

    private val eligibilityFailMessage: ImageArticleModel = ImageArticleModel(
        id = "eligibility",
        title = "Você não é elegível para o estudo.",
        description = "Por favor, verifique novamente mais tarde e fique atento para mais estudos em breve!",
        drawableId = drawable.sample_image_alpha1
    )

    private val eligibilityQuestions: List<QuestionModel<Any>> = listOf(
        ChoiceQuestionModel(
            "age",
            "Qual é a sua idade?",
            candidates = (20..50).toList(),
            viewType = Dropdown
        ),
        ChoiceQuestionModel(
            "gender",
            "Qual é o seu gênero?",
            candidates = listOf("Masculino", "Feminino"),
        ),
        ChoiceQuestionModel(
            "hasCardiac",
            "Você tem alguma condição cardíaca existente?",
            "Exemplos de condições cardíacas incluem batimentos cardíacos anormais ou arritmias.",
            candidates = listOf("Sim", "Não"),
            answer = "Sim"
        ),
        ChoiceQuestionModel(
            "hasWearableDevice",
            "Você possui atualmente um dispositivo wearable?",
            "Exemplos de dispositivos wearable incluem Samsung Galaxy Watch 4, Fitbit, OuraRing, etc.",
            candidates = listOf("Sim", "Não"),
            answer = "Sim"
        )
    ) as List<QuestionModel<Any>>
}
