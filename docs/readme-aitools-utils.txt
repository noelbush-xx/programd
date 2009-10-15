aitools utils

This is a collection of utilities that I built up over the last couple of years, and which I
found I was frequently reusing, in Program D as well as elsewhere, and which I realized had
nothing to do with the "core" tasks of Program D (pattern matching, template parsing, and
the other aspects of AIML interpreter implementation).  I've moved them to this separate
library to try to avoid duplicating functionality in several different places, and also as
part of a general effort to try to find well-maintained replacements for most or all of these
tools.

I wouldn't necessarily advise building a project that relies on these utilities.  Although you
are, of course, free to do so, I don't make any promises about supporting the library.  In fact,
I will admit up front that I feel particularly willing to change just about anything here,
public interface-wise as well as behavior, as it suits the needs of Program D and other tools.
I look at most of these utilities as stand-ins for something better that's got to be out there.
I'd be grateful for any pointers to any well-maintained libraries that can do the things I need
here, which are almost entirely about filling in little gaps and cracks in what comes with the
standard JDK.

Noel Bush <noel@aitools.org>
April 2006
