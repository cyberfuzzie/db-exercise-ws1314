/*Die Ueberhangmandate berechnen sich aus der Differenz aus Anzahl Sitze durch Direktmandate
  und Anzahl Sitze durch Zweitstimmen*/
create or replace view Ueberhangmandate as(
    select m.bundeslandid, m.parteiid, greatest(0, m.sitze-l.sitze) AnzahlUeberhangmandate
    from mindestanzahlsitze m join listenergebnis l on m.bundeslandid = l.bundeslandid
                                   and l.parteiid = m.parteiid
);

/*Output-View*/
create or replace view Output_Ueberhangmandate as(
    select b.name Bundesland, p.name Partei, anzahlueberhangmandate
    from Ueberhangmandate u join bundesland b on u.bundeslandid = b.bundeslandid
                            join partei p on u.parteiid = p.parteiid
);