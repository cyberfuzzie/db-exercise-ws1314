create or replace view Ueberhangmandate as(
    select m.bundeslandid, m.parteiid, greatest(0, m.sitze-l.sitze) AnzahlUeberhangmandate
    from mindestanzahlsitze m join listenergebnis l on m.bundeslandid = l.bundeslandid
                                   and l.parteiid = m.parteiid
);