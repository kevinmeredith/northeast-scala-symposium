package nescala.boston

import nescala.Meetup

trait Templates extends nescala.Templates {
  import java.net.URLEncoder.encode

  def bostonLayout(head: xml.NodeSeq)
    (bodyScripts: xml.NodeSeq)
    (body: xml.NodeSeq) = unfiltered.response.Html(
      <html>
      <head>
        <meta http-equiv="Content-Type" content="text/html;charset=utf-8" />
        <title>&#8663;northeast scala symposium</title>
        <link rel="stylesheet" type="text/css" href="http://fonts.googleapis.com/css?family=Arvo:regular,bold"/>
        <link rel="stylesheet" type="text/css" href="/css/tipsy.css" />
        <link rel="stylesheet" type="text/css" href="/facebox/facebox.css"/>
        <link rel="stylesheet" type="text/css" href="/css/boston.css" />
        <script type="text/javascript" src="https://ajax.googleapis.com/ajax/libs/jquery/1.7.1/jquery.min.js"></script>
        { head }
      </head>
      <body>
        { body }
        <div id="footer">
          made possible with <span class="love">&#10084;</span> from the <a href="http://www.meetup.com/boston-scala/">Boston</a>, <a href="http://www.meetup.com/scala-phase/">Philadelphia</a>, and <a href="http://www.meetup.com/ny-scala/">New York</a> scala enthusiasts
          <div id="last-year">
            <div>What happen to last year? It ended.</div>
            <div>But you can still find it <a href="/2011">here</a>.</div>
            <a href="/2011"><img src="/images/ne.png"/></a>
          </div>
          <div id="lets">2012 let's make more awesome</div>
        </div>
      <script type="text/javascript" src="/facebox/facebox.js"></script>
      <script type="text/javascript" src="/js/jquery.tipsy.js"></script>
      <script type="text/javascript" src="/js/boston/boston.js"></script>
      { bodyScripts }
      </body>
    </html>
  )

  def tallied(authed: Boolean, total: Int, entries: Seq[Map[String, String]], kind: String) =
    bostonLayout(
      <script type="text/javascript" src="/js/tally.js"></script>)(<link rel="stylesheet" type="text/csss" href="/css/tally.css"/>)({
        head(authed, kind)
      } ++ <div class="contained"> { if(authed) {
         <p><strong>{total}</strong> votes submitted so far</p>
         <ul data-total={ total.toString } id="tallies">{ entries map { e =>
           <li class="clearfix" title={ e.get("mu_name").getOrElse("mu_name") } id={"e-%s" format e("id") }
            data-score={ ((e("votes").toDouble / total) * 100).toString }>
            <img class="avatar" src={  e("mu_photo").replace("member_", "thumb_") } />
            <div class="progress clearfix"><span class="bar">.</span>
            <span class="title">{ e("name") } <strong>{ e("votes") }</strong></span></div>
          </li>
        } }</ul> } else {
          <p>No Peeking. Login to view tally</p>
       } }</div>)

  def indexNoAuth = index(false)

  def indexWithAuth(proposals: Seq[Map[String, String]], panels: Seq[Map[String, String]]) =
    index(true, proposals, panels)

  def panelList(props: Seq[Map[String, String]]) =
    listOf(props, "panel_proposals", Panels.MaxTalkName,
           Panels.MaxTalkDesc, "Your panel proposals", "Edit Panel", "#propose-panel-form")

  def proposalList(props: Seq[Map[String, String]]) =
    listOf(props, "proposals", Proposals.MaxTalkName,
           Proposals.MaxTalkDesc, "Your talk proposals", "Edit Talk", "#propose-form")

  def listOf(props: Seq[Map[String, String]], kind: String, maxName: Int,
             maxDesc: Int, listTitle: String, editLabel: String, sourceForm: String) =
    <div id={ kind }>
      <h5 class="proposal-header">{ listTitle }</h5>
       <ul id={ "%s-list" format kind }>
       {
         props.map { p =>
         <li id={ p("id") }>
           <form action={"/boston/%s/%s" format (kind, encode(p("id"), "utf8")) }
                    method="POST" class="propose-edit-form">
             <div>
               <a href="#" class="toggle name" data-val={ p("name") }>{ p("name") }</a>
               <input type="text" name="name" maxlength={ maxName + "" } value={ p("name") } />
             </div>
             <div class="preview">
               <div class="controls clearfix">
                 <ul>
                   <li>
                    <a href="mailto:doug@meetup.com">Email us</a> if you with to withdraw this.
                   </li>
                   <li>
                      <a href="#" class="edit-proposal" data-proposal={ p("id") }>
                        edit
                      </a>
                    </li>
                 </ul>
               </div>
               <div class="linkify desc" data-val={ p("desc") }>{ p("desc") }</div>
               <div class="edit-desc limited">
                  <textarea data-limit={ maxDesc + "" } name="desc">{ p("desc") }</textarea>
                  <div class="form-extras">
                    <div class="limit-label"/>
                    <div class="edit-controls clearfix">
                      <input type="submit" value={ editLabel } class="btn" />
                      <input type="button" value="Cancel" class="btn cancel" />
                    </div>
                  </div>
                </div>
             </div>
           </form>
         </li>
         }
       }
     </ul>
    </div>
  
  val twttrFollow = {
    <a href="https://twitter.com/nescalas" class="twitter-follow-button" data-show-count="false" data-lang="en" data-size="large">Follow @nescalas</a>
  }

  def login(authed: Boolean, then: String) =
    if(!authed) <div id="auth-bar" class="clearfix"><div class="contained"><div class="l">Just who are you anyway?</div><div class="r"><a href={ "/connect%s" format(if(then.isEmpty) "" else "?then=%s".format(then)) } class="btn login">Log in with Meetup</a></div></div></div> else <span/>

  def head(authed: Boolean, afterlogin: String = "") =
   <div id="head" class="clearfix">
    <div class="contained">
      <div class="l">
        <a href="/"><h1>&#8663;northeast<span>scala</span>symposium</h1></a>
      </div>
      <div class="r">
        <h2>Boston</h2>
        <h4>functionally typed party</h4>
        { twttrFollow }
      </div>
    </div>
   </div> ++ { login(authed, afterlogin) }

  // listing of talk proposals (refactor plz)
  def talkListing(
    proposals: Seq[Map[String, String]],
    authed: Boolean = false,
    votes: Seq[String] = Seq.empty[String]) = bostonLayout(Nil)(Nil)({
      head(true/*hide login*/, "vote-for-talk")
    } ++ <div class="contained">
     <div id="maybe-talks-header">
        <h2>{ proposals.size } Scala campfire stories</h2>
        <div>This year's symposium will feature 16 talks and one <a href="/2012/panels">panel</a> from members of the Scala community. Below is a list of current talk proposals.</div>
      </div>
      <ul>{
        proposals.map { p =>
        <li class="talk" id={ p("id").split(":")(3) }>
          <h2><a href={ "#"+p("id").split(":")(3) }>{ p("name") }</a></h2>
          <div class="who-box clearfix">
            <img class="avatar" src={ p("mu_photo").replace("member_", "thumb_") } />
            <div class="links">
              <a class="primary" href={ "http://meetup.com/nescala/members/%s" format p("id").split(":")(2) } target="_blank">{ p("mu_name") } </a>{ if(p.isDefinedAt("twttr")) {
                  <a class="twttr" href={ "http://twitter.com/%s" format p("twttr").drop(1) } target="_blank">{ p("twttr") }</a>
                } else <span/> }
            </div>
          </div>
          <p class="desc">{ p("desc") }</p>
        </li>
        }
      }</ul>
    </div>
  )

  // listing of panel proposals (refactor plz)
  def panelListing(proposals: Seq[Map[String, String]], authed: Boolean = false,
                   votes: Seq[String] = Seq.empty[String]) = bostonLayout(Nil)(Nil)({
    head(true/*hide login*/, "vote-for-panel")
  } ++ <div class="contained">
      <div id="maybe-talks-header">
        <h2>{ proposals.size } Scala Panel { if(proposals.size == 1) "Discussion" else "Discussons" }</h2>
        <div>In addition to a number of <a href="/2012/talks">talks</a>, this year's symposium will feature one panel discussion among peers. Below is a list of current panel proposals.</div>
      </div>
      <ul>{
        proposals.map { p =>
        <li class="talk" id={ p("id").split(":")(3) }>
          <h2><a href={ "#"+p("id").split(":")(3) }>{ p("name") }</a></h2>
          <div class="who-box clearfix">
            <img class="avatar" src={ p("mu_photo").replace("member_", "thumb_") } />
            <div class="links">
              <a class="primary" href={ "http://meetup.com/nescala/members/%s" format p("id").split(":")(2) } target="_blank">{ p("mu_name") } </a>{ if(p.isDefinedAt("twttr")) {
                  <a class="twttr" href={ "http://twitter.com/%s" format p("twttr").drop(1) } target="_blank">{ p("twttr") }</a>
                } else <span/> }
            </div>
          </div>
          <p class="desc">{ p("desc") }</p>
        </li>
        }
      }</ul>
    </div>
  )


  val rsvps =
    <div class="attending">
      <h4 class="tban"/><ul class="rsvps"/><p class="extra-rsvps"/>
    </div>

 def dayOne(authed: Boolean, proposals: Seq[Map[String, String]], panels: Seq[Map[String, String]]) = 
   <div id="day-one" data-event={ Meetup.Boston.dayone_event_id } class="day clearfix">
      <div class="contained">
        <div id="talk-submissions">
          <div class="l">
            <h1><a href="http://www.meetup.com/nescala/events/37637442/">Day 01</a></h1>
            <h2>3.09.12</h2>
            <h3>
              <span>9am @<a href="http://maps.google.com/maps?q=One+Memorial+Drive%2C+Cambridge%2C+MA">NERD</a></span>
            </h3>
            <p>Scala <a href="/2012/talks">Talks</a> and <a href="/2012/panels">Panels</a></p>{
              rsvps
            }
          </div>
          <div class="r">
            <h1>Polls are now <a href="/2012/talks">Closed</a></h1>
            <p>This year's symposium features 16 talks of 30 minutes, one keynote talk, and one 45 - 60 minute panel discussion.</p>
            <p>Thanks to all attendees who voted. The results will be posted soon.</p>           
          </div>
        </div>
      </div>
    </div>

  private val dayTwo =
    <div id="day-two" data-event={ Meetup.Boston.daytwo_event_id } class="day clearfix">
      <div class="contained">
        <div class="l">
          <h1><a href="http://www.meetup.com/nescala/events/44042982/" target="_blank">Day 02</a></h1>
          <h2>3.10.12</h2>
          <h3>
            <span>10am @<a href="http://maps.google.com/maps?q=32+Vassar+Street%2C+Cambridge%2C+MA">Stata Center</a></span>
          </h3>
          <p>Scala Workshops</p>
          { rsvps }
        </div>
        <div class="r">
          <p>
            The second day of the symposium is hands-on Scala hacking and workshops
            hosted at MIT.
          </p>
        </div>
      </div>
    </div>

  private val dayThree =
    <div id="day-three" data-event={ Meetup.Boston.daythree_event_id }
      class="day clearfix">
      <div class="contained">
        <div class="l">
          <h1><a href="http://www.meetup.com/nescala/events/44049692/">Day 03</a></h1>
          <h2>3.11.12</h2>
          <h3>
            <span>10am @<a href="http://maps.google.com/maps?q=32+Vassar+Street%2C+Cambridge%2C+MA">Stata Center</a></span>
          </h3>
          { rsvps }
        </div>
        <div class="r">No details yet</div>
      </div>
    </div>

  private def index(
    authed: Boolean,
    proposals: Seq[Map[String, String]] = Nil,
    panels: Seq[Map[String, String]] = Nil) =
    bostonLayout(Nil)(
    <script type="text/javascript" src="/js/jquery.scrollTo-1.4.2-min.js"></script>
    <script type="text/javascript" src="/js/boston/index.js"></script>)(
      head(true/*hide login*/) ++ dayOne(authed, proposals, panels) ++ dayTwo ++ dayThree
    )
}

object Templates extends Templates {}
