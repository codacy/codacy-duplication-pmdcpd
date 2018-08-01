create or replace package body pljson_parser as
  /*
  Copyright (c) 2009 Jonas Krogsboell

  Permission is hereby granted, free of charge, to any person obtaining a copy
  of this software and associated documentation files (the "Software"), to deal
  in the Software without restriction, including without limitation the rights
  to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
  copies of the Software, and to permit persons to whom the Software is
  furnished to do so, subject to the following conditions:

  The above copyright notice and this permission notice shall be included in
  all copies or substantial portions of the Software.

  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
  IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
  FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
  AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
  LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
  OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
  THE SOFTWARE.
  */

  /* type json_src is record (len number, offset number, src varchar2(32767), s_clob clob); */
  /* assertions
    offset: contains 0-base offset of buffer,
      so 1-st entry is offset + 1, 4000-th entry = offset + 4000
    src: contains offset + 1 .. offset + 4000, ex. 1..4000, 4001..8000, etc.
  */
  function next_char(indx number, s in out nocopy json_src) return varchar2 as
  begin
    if(indx > s.len) then return null; end if;
    --right offset?
    /*  if(indx > 4000 + s.offset or indx < s.offset) then */
    /* fix for issue #37 */
    if(indx > 4000 + s.offset or indx <= s.offset) then
    --load right offset
      s.offset := indx - (indx mod 4000);
      /* addon fix for issue #37 */
      if s.offset = indx then
        s.offset := s.offset - 4000;
      end if;
      s.src := dbms_lob.substr(s.s_clob, 4000, s.offset+1);
    end if;
    --read from s.src
    return substr(s.src, indx-s.offset, 1);
  end;

end pljson_parser;
/
