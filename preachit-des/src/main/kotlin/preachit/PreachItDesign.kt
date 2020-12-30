package preachit

import ee.design.*
import ee.lang.*

/*
    "Gesamteindruck" : [
        {
            "keyword": "Aufmerksamkeit",
            "question": "Die Predigt hat meine Aufmerksamkeit:",
            "rating": {
                "type": "linear",
                "scala": ["nicht geweckt", "zum Teil", "überwiegend", "gepackt", "durchgehend"]
            }
        }, {
            "keyword": "Vorbereitung",
            "question": "Auf mich wirkte die Predigt:",
            "rating": {
                "type": "linear",
                "scala": ["unvorbereitet", "eher unvorb.", "vorber.", "gut vorber.", "sehr gut vorber."]
            }
        }
    ],
 */

object PreachIt : Module({ namespace("preachit") }) {
    object Survey : Entity() {
        val label = propS()
        val questionGroups = prop(n.List.GT(QuestionGroup))
        val note = prop(n.Text)
    }

    object Rating : Basic() {
        val type = propS()
        val scala = prop(n.List.GT(n.String))
    }

    object Question : Basic() {
        val keyword = propS()
        val text = propS()
        val rating = prop(Rating)
    }

    object QuestionGroup : Values() {
        val label = propS()
        val questions = prop(n.List.GT(Question))
    }

    object SurveyRating : Entity() {
        val label = propS()
        val person = prop(PersonName)
    }

    object PersonName : Basic() {
        val first = propS()
        val last = propS()
    }

    object UserCredentials : Values() {
        val username = propS()
        val password = propS()
    }

    object Account : Entity() {
        val name = prop(PersonName)
        val username = propS().unique()
        val password = propS().hidden()
        val email = propS().unique()
        val roles = propListT(n.String)

        val disabled = propB().meta()

        val login = command(username, email, password)
        val enable = updateBy(p(disabled) { value(false) })
        val disable = updateBy(p(disabled) { value(true) })

        val sendCreatedConfirmation = command()
        val sendEnabledConfirmation = command()
        val sendDisabledConfirmation = command()

        object Handler : AggregateHandler() {
            object Initial : State({
                executeAndProduce(commandCreate())
                handle(eventOf(commandCreate())).ifTrue(disabled.yes()).to(Disabled)
                handle(eventOf(commandCreate())).ifFalse(disabled.yes()).to(Enabled)
            })

            object Exist : State({
                virtual()
                executeAndProduce(commandUpdate())
                handle(eventOf(commandUpdate()))

                executeAndProduce(commandDelete())
                handle(eventOf(commandDelete())).to(Deleted)
            })

            object Disabled : State({
                superUnit(Exist)
                executeAndProduce(enable)
                handle(eventOf(enable)).to(Enabled)
            })

            object Enabled : State({
                superUnit(Exist)
                executeAndProduce(disable)
                handle(eventOf(disable)).to(Disabled)
            })

            object Deleted : State()
        }

        object AccountConfirmation : ProcessManager() {
            object Initial : State({
                executeAndProduce(commandCreate())
                handle(eventOf(commandCreate())).ifTrue(disabled.yes()).to(Disabled)
                handle(eventOf(commandCreate())).ifFalse(disabled.yes()).to(Enabled)
            })

            object Disabled : State({
                handle(eventOf(enable)).to(Enabled).produce(sendEnabledConfirmation)
            })

            object Enabled : State({
                handle(eventOf(disable)).to(Disabled).produce(sendDisabledConfirmation)
            })
        }
    }

}