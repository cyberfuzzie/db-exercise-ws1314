create or replace view Ueberhangmandate as(
    select m.bundeslandid, m.parteiid, greatest(0, m.sitze-l.sitze) AnzahlUeberhangmandate
    from mindestanzahlsitze m join listenergebnis l on m.bundeslandid = l.bundeslandid
                                   and l.parteiid = m.parteiid
);

create or replace view Output_Ueberhangmandate as(
    select b.name Bundesland, p.name Partei, anzahlueberhangmandate
    from Ueberhangmandate u join bundesland b on u.bundeslandid = b.bundeslandid
                            join partei p on u.parteiid = p.parteiid
);